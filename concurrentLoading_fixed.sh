#!/bin/bash

# Variables
PASSWORD="computacion"  # Contraseña para SSH y SCP (considera usar claves SSH)
USER="computacion"
REMOTE_PATH="/home/$USER/Documents/RicardoUrbina"
LOCAL_FILE="./client/build/libs/client.jar"
SERVERS=("xhgrid7" "xhgrid8")  # Servidores a los que se conectará

# Iterar sobre cada servidor
for SERVER in "${SERVERS[@]}"
do
    (
        echo "[$SERVER] Asegurando que el directorio $REMOTE_PATH existe..."
        # Crear el directorio remoto si no existe
        sshpass -p "$PASSWORD" ssh -o StrictHostKeyChecking=no $USER@$SERVER "mkdir -p $REMOTE_PATH"
        if [ $? -ne 0 ]; then
            echo "[$SERVER] Error al crear el directorio remoto."
            exit 1
        fi

        echo "[$SERVER] Transfiriendo $LOCAL_FILE..."
        # Enviar el archivo con scp usando sshpass para automatizar la contraseña
        sshpass -p "$PASSWORD" scp -o StrictHostKeyChecking=no $LOCAL_FILE $USER@$SERVER:$REMOTE_PATH
        if [ $? -ne 0 ]; then
            echo "[$SERVER] Error al transferir el archivo."
            exit 1
        fi

        echo "[$SERVER] Ejecutando client.jar con 1000 peticiones..."
        # Conectar por ssh y ejecutar client.jar
        sshpass -p "$PASSWORD" ssh -o StrictHostKeyChecking=no $USER@$SERVER << EOF
cd $REMOTE_PATH
for ((j=1; j<=3; j++))
do
    echo "Ejecutando petición \$j de 1000"
    # Ejecutar el client.jar y enviar las entradas necesarias
    echo -e "1\nto xhgrid7 micoño" | java -jar client.jar
    sleep 4
done
EOF
        if [ $? -ne 0 ]; then
            echo "[$SERVER] Error al ejecutar client.jar."
            exit 1
        fi

        echo "[$SERVER] Proceso completado con 1000 peticiones."
    ) &
done

# Esperar a que todos los procesos en segundo plano finalicen
wait
echo "Todos los procesos han sido completados."
