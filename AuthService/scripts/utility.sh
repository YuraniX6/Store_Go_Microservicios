#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Script utilities para auth-service

show_menu() {
    echo -e "${BLUE}=== StoreGo Auth Service Utility ===${NC}"
    echo "1. Build (compilar)"
    echo "2. Test (ejecutar tests)"
    echo "3. Run (ejecutar localmente)"
    echo "4. Docker Build"
    echo "5. Docker Compose Up"
    echo "6. Docker Compose Down"
    echo "7. Clean (limpiar build)"
    echo "8. Reset Database"
    echo "9. View Swagger"
    echo "10. Health Check"
    echo "11. Exit"
    echo -n "Seleccionar opción: "
}

build() {
    echo -e "${YELLOW}Compilando...${NC}"
    mvn clean install
    echo -e "${GREEN}Build completado!${NC}"
}

test() {
    echo -e "${YELLOW}Ejecutando tests...${NC}"
    mvn test
    echo -e "${GREEN}Tests completados!${NC}"
}

run() {
    echo -e "${YELLOW}Iniciando aplicación...${NC}"
    mvn spring-boot:run
}

docker_build() {
    echo -e "${YELLOW}Construyendo imagen Docker...${NC}"
    docker build -t auth-service:latest .
    echo -e "${GREEN}Imagen construida!${NC}"
}

docker_up() {
    echo -e "${YELLOW}Iniciando docker-compose...${NC}"
    docker-compose up -d
    echo -e "${GREEN}Docker compose iniciado!${NC}"
    echo "App disponible en: http://localhost:8080"
    echo "Swagger en: http://localhost:8080/swagger-ui.html"
}

docker_down() {
    echo -e "${YELLOW}Deteniendo docker-compose...${NC}"
    docker-compose down
    echo -e "${GREEN}Docker compose detenido!${NC}"
}

clean() {
    echo -e "${YELLOW}Limpiando build...${NC}"
    mvn clean
    rm -rf target/
    echo -e "${GREEN}Limpieza completada!${NC}"
}

reset_db() {
    echo -e "${YELLOW}Reseteando base de datos...${NC}"
    docker-compose down -v
    docker-compose up -d
    sleep 10
    echo -e "${GREEN}BD reseteada!${NC}"
}

view_swagger() {
    if command -v xdg-open > /dev/null; then
        xdg-open http://localhost:8080/swagger-ui.html
    elif command -v open > /dev/null; then
        open http://localhost:8080/swagger-ui.html
    else
        echo "Abrir manualmente: http://localhost:8080/swagger-ui.html"
    fi
}

health_check() {
    echo -e "${YELLOW}Verificando salud de la aplicación...${NC}"
    
    RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/swagger-ui.html)
    
    if [ $RESPONSE -eq 200 ]; then
        echo -e "${GREEN}✓ API está corriendo (HTTP $RESPONSE)${NC}"
        
        # Test login con admin
        LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/auth/login \
          -H "Content-Type: application/json" \
          -d '{"username":"admin","password":"admin123"}')
        
        if echo "$LOGIN_RESPONSE" | grep -q "token"; then
            echo -e "${GREEN}✓ Login funcionando${NC}"
        else
            echo -e "${YELLOW}✗ Login error${NC}"
        fi
    else
        echo -e "${YELLOW}✗ API no está disponible (HTTP $RESPONSE)${NC}"
    fi
}

# Main loop
while true; do
    show_menu
    read choice
    
    case $choice in
        1) build ;;
        2) test ;;
        3) run ;;
        4) docker_build ;;
        5) docker_up ;;
        6) docker_down ;;
        7) clean ;;
        8) reset_db ;;
        9) view_swagger ;;
        10) health_check ;;
        11) exit 0 ;;
        *) echo -e "${YELLOW}Opción inválida${NC}" ;;
    esac
    
    echo ""
    read -p "Presionar Enter para continuar..."
done
