# ToDo Hub


> The purpose of this application is to create tasks (ToDos) and assign them to specific persons (Assignees). The main features are the following: 
- Create Assignees 
- Create ToDos with optional allocation to Assignees
- Edit ToDos and Assignees 
- Delete ToDos and Assignees
- Mark ToDos as finished 

> This application provides Java RESTful API with Spring Boot, MariaDB and
> single page application frontend with Vue.js v3

## Prerequisites

### API
- [Install a JDK](https://www.oracle.com/de/java/technologies/downloads/) in at least version 21.
- Make sure that `JAVA_HOME` is set correctly to the root directory of your JDK. You can check with this command: `echo %JAVA_HOME%` (or `echo $JAVA_HOME` on Linux / Git Bash) (or `echo $Env:JAVA_HOME` in Powershell)
- Make sure that the JDK `bin` folder is added to your `PATH`. You can check with this command: `echo %PATH%` (or `echo $PATH` on Linux / Git Bash) (or `echo $Env:PATH` in Powershell)
- You can also install [Maven](https://maven.apache.org/) yourself, but if you use the Maven Wrapper scripts (`mvnw` or `mvnw.cmd`), this is not necessary, since Maven will be downloaded in this case
- Install and start a local [MariaDB server](https://mariadb.org/download/). You can instead use the [docker-compose.yml](../docker-compose.yml). If necessary, update the [API configuration file](src/main/resources/application.properties) with your database port (default is `3306`) and credentials (default is `root`:`root`)
### Frontend
- Install [Node.js](https://nodejs.org/en/) and ensure that the root folder of its installation is added to your PATH. You can check with this command: echo %PATH% (or echo $PATH on Linux / Git Bash).


## Quick Start (API)

1. Clone this repository.
2. Start the API by navigating into its folder and executing 
    - `./mvnw spring-boot:run`

### Usage
NOTE: every request uses http://localhost:8080/api/v1 as prefix

### available ressources (HTTP-Client of your choice)

- GET    `/assignees`
- GET   `  /assignees/$id`
- POST   `/assignees`
- PUT   ` /assignees/$id`
- DELETE` /assignees/$id`
######
-  GET   ` /todos`
-  GET  `  /todos/$id`
- POST  ` /todos`
-  PUT   ` /todos/$id`
-  DELETE `/todos/$id`

## Full Application (API + Frontend)
### Follow the API Quick Start guide 

### Install the required dependencies with
```sh
npm install
```

### Then you are good to go with
```sh
npm run dev
```

### To deploy your code to production, you can compile and minify using
```sh
npm run build
```

### Also, there is linter available:
```sh
npm run lint
```
## Documentation

- General references: https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle
- Application properties: https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-application-properties.html

## Screenshots
### ToDos
<img width="2552" height="1131" alt="image" src="https://github.com/user-attachments/assets/b93042a7-ed32-437d-981f-de9c7373a06d" />
<img width="2545" height="1137" alt="image" src="https://github.com/user-attachments/assets/52a0ac37-7269-44d4-b39b-8d52acb4bdc2" />
<img width="2535" height="1142" alt="image" src="https://github.com/user-attachments/assets/3fccf84c-d4d4-4ef0-9756-a61db160037b" />
<img width="2546" height="1151" alt="image" src="https://github.com/user-attachments/assets/c09f7fcc-be65-40fa-8129-f7d01e97ab7b" />
<img width="2556" height="1123" alt="image" src="https://github.com/user-attachments/assets/02751e8d-0827-4583-86c7-892e6b1f6fba" />


### Assignees
<img width="2557" height="632" alt="image" src="https://github.com/user-attachments/assets/6b468450-9a4b-4aa8-a98e-ccd4b8619f51" />
<img width="2550" height="726" alt="image" src="https://github.com/user-attachments/assets/7bb5f0fe-ef1a-4a0a-8c4c-6ebeb838cbee" />















