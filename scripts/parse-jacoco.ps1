Param(
  [string]$InputPath = "./jacoco.xml",
  [string]$OutputPath = "./jacoco-methods.json"
)

$xml = [xml](Get-Content -Raw $InputPath)
$result = @()
foreach ($pkg in $xml.report.package) {
  foreach ($cls in $pkg.class) {
    $className = ($pkg.name + '.' + $cls.name) -replace '/', '.'
    foreach ($m in $cls.method) {
      $instrMiss = 0; $branchMiss = 0; $lineMiss = 0; $compMiss = 0
      foreach ($c in $m.counter) {
        switch ($c.type) {
          'INSTRUCTION' { $instrMiss = [int]$c.missed }
          'BRANCH'      { $branchMiss = [int]$c.missed }
          'LINE'        { $lineMiss = [int]$c.missed }
          'COMPLEXITY'  { $compMiss = [int]$c.missed }
        }
      }
      if (($instrMiss + $branchMiss + $lineMiss + $compMiss) -gt 0) {
        $result += [pscustomobject]@{
          Class=$className; Method=$m.name; Desc=$m.desc; Line=[int]$m.line;
          InstrMiss=$instrMiss; BranchMiss=$branchMiss; LineMiss=$lineMiss; CompMiss=$compMiss
        }
      }
    }
  }
}

$result | ConvertTo-Json -Depth 3 | Out-File -FilePath $OutputPath -Encoding UTF8
Write-Output ("Wrote {0} records to {1}" -f $result.Count, $OutputPath)
