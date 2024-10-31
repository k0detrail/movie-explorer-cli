# Prerequisites

- Java Development Kit (JDK)
- Node.js

# Getting Started

## Clone the Repository

Clone the repository to the local machine:

```shell
git https://github.com/k0detrail/movie-explorer-cli.git
cd movie-explorer-cli
```

## Compile the Project

To compile the Java source files, run the following command:

```shell
javac -cp "libs/json-20240303.jar:libs/dotenv-java-3.0.2.jar" Main.java
```

## Run the Application

Once compiled, run the application with:

```shell
java -cp "libs/json-20240303.jar:libs/dotenv-java-3.0.2.jar" Main
```

> **Note:** Do not include the .java extension when running the Java class.

# Code Formatting

We use Prettier to maintain code quality and consistency.

## Install Prettier and Java Plugin

Install Prettier and the Prettier Java plugin:

```shell
npm install prettier-plugin-java --save-dev
```

## Check Code Formatting

Check the formatting of the Java files:

```shell
npx prettier --check .
```

## Format Code

Format all Java files in the project:

```shell
npm run format
```
