@echo off

if "%1"=="" goto help
if "%1"=="test" goto test
if "%1"=="test-unit" goto test-unit
if "%1"=="test-integration" goto test-integration
if "%1"=="run" goto run
if "%1"=="build" goto build
if "%1"=="help" goto help

:test
echo Running all tests...
go test -v ./... -run "^Test[^Integration]"
go test -v ./... -tags=integration -run "^TestIntegration"
goto end

:test-unit
echo Running unit tests...
go test -v ./... -run "^Test[^Integration]"
goto end

:test-integration
echo Running integration tests...
go test -v ./... -tags=integration -run "^TestIntegration"
goto end

:run
echo Starting service...
go run main.go
goto end

:build
echo Building service...
go build -o monitoring-service.exe .
goto end

:help
echo Usage: test.bat [command]
echo Commands:
echo   test            Run all tests
echo   test-unit      Run unit tests only
echo   test-integration Run integration tests only
echo   run            Run the service
echo   build          Build the service
goto end

:end