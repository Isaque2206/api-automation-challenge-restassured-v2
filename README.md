# API Automation Challenge – RestAssured + JUnit 5

[![CI](https://github.com/<owner>/<repo>/actions/workflows/ci.yml/badge.svg)](https://github.com/<owner>/<repo>/actions/workflows/ci.yml)

Testes de API com **Rest-Assured** e **JUnit 5**, relatórios **Allure**, e pipeline de CI.

## Stack

- Java 11
- Maven
- JUnit 5
- Rest-Assured
- Allure (JUnit5 + Rest-Assured)
- GitHub Actions (CI)  
  > Alternativas: Jenkins / GitLab CI (arquivos prontos neste repo)

## Como rodar localmente

```bash
# 1) Clonar
git clone https://github.com/<owner>/<repo>.git
cd <repo>

# 2) Rodar testes (gera target/allure-results)
mvn clean test

# 3) Gerar e abrir o relatório Allure local
mvn allure:serve
# ou gerar HTML sem abrir:
mvn allure:report
# abrir manualmente: target/site/allure-maven-plugin/index.html
