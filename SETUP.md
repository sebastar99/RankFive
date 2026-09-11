## Cómo levantar el proyecto localmente

### Requisitos
- Java 25 (¡importante! Versiones más nuevas como 26 pueden causar errores
  de compilación por incompatibilidad con el plugin de PMD)
- Maven
- Docker Desktop (tiene que estar corriendo)

### Antes de la primera vez
Si tenés un MySQL corriendo local en tu compu (fuera de Docker), frenalo,
porque usa el mismo puerto (3306) que necesita el contenedor:

net stop MySQL80
(como administrador — si no usás MySQL como servicio de Windows, saltear este paso)

### Levantar el proyecto (primera vez o con código nuevo)
git pull
mvn clean package
docker-compose up --build


### Día a día (sin cambios de código)
docker-compose up


### Frenar
docker-compose down


### Acceder
http://localhost:8080/spring



La configuración de base de datos ya está en el `.env` versionado en el repo,
no hace falta armar nada a mano.


