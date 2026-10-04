Param(
  [string]$JsonPath = "./jacoco-methods.json",
  [string]$OutPath = "./jacoco-methods.md"
)

$items = Get-Content -Raw $JsonPath | ConvertFrom-Json
$groups = $items | Group-Object Class
$sb = New-Object System.Text.StringBuilder
$null = $sb.AppendLine("## Detalle por método con faltantes")
foreach ($g in $groups) {
  $null = $sb.AppendLine("")
  $null = $sb.AppendLine("### " + $g.Name)
  foreach ($m in $g.Group) {
    $line = ("- {0} {1} (línea {2}): instr={3}, ramas={4}, líneas={5}, complejidad={6}" -f $m.Method, $m.Desc, $m.Line, $m.InstrMiss, $m.BranchMiss, $m.LineMiss, $m.CompMiss)
    $null = $sb.AppendLine($line)
  }
}
[IO.File]::WriteAllText($OutPath, $sb.ToString(), [Text.Encoding]::UTF8)
