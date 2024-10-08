#!/bin/bash
PASSWORD="computacion"  # Contraseña para SSH y SCP
# Definir el rango de máquinas a las que quieres conectarte (xhgrid4 a xhgrid10)
for i in {7..9}
do
    (
        SERVER="xhgrid$i"
        USER="computacion"
        REMOTE_PATH="/home/$USER/Documents/RicardoUrbina"
        LOCAL_FILE="./client/build/libs/client.jar"

        echo "Asegurando que el directorio $REMOTE_PATH existe en $SERVER..."
        # Crear el directorio remoto si no existe
        sshpass -p "$PASSWORD" ssh -o StrictHostKeyChecking=no $USER@$SERVER -t "mkdir -p $REMOTE_PATH"

        echo "Transfiriendo $LOCAL_FILE a $SERVER..."
        # Enviar el archivo con scp usando sshpass para automatizar la contraseña
        sshpass -p "$PASSWORD" scp -o StrictHostKeyChecking=no $LOCAL_FILE $USER@$SERVER:$REMOTE_PATH

        echo "Conectando a $SERVER y ejecutando client.jar con 1000 peticiones..."
        # Conectar por ssh, ejecutar client.jar y enviar las entradas automáticas 1000 veces usando sshpass
        sshpass -p "$PASSWORD" ssh -o StrictHostKeyChecking=no -t $USER@$SERVER << EOF
        cd $REMOTE_PATH
        for ((j=1; j<=2; j++))
        do
            echo "Ejecutando petición \$j de 1000"
            # Ejecutar el client.jar y enviar las entradas 1 y 100000 cuando se soliciten
            echo -e "1\n200" | java -jar client.jar
        done
EOF
        echo "Proceso completado en $SERVER con 1000 peticiones."
    ) &
done
# Esperar a que todos los procesos en segundo plano finalicen
wait
echo "Todos los procesos han sido completados."
