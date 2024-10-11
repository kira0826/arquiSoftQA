import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.zeroc.Ice.Current;

import Demo.CallBackPrx;
import Demo.Response;
import responses.PendingResponse;
import responses.PendingResponseManager;
import responses.PendingResponseSequential;
import responses.UpdateMessagesJob;
import utils.ProxiesManager;

public class ChatHandlerI implements Demo.ChatHandler {

    // Inicializar el ExecutorService con un pool fijo basado en los núcleos disponibles
    private final ExecutorService executorService = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors()
    );

    @Override
    public Response broadcast(String hostname, String message, Current current) {
        // Crear una respuesta inmediata
        Response response = new Response();
        response.value = "Broadcast solicitado. Procesando en segundo plano.";

        // Enviar la tarea al thread pool
        executorService.execute(() -> {
            try {
                ProxiesManager.getInstance().getAllProxies().forEach((k, v) -> {
                    CallBackPrx callBackPrx = v;

                    if (callBackPrx != null) {
                        String messageResponse = "Message from: " + hostname + " | Message: " + message;
                        Response innerResponse = new Response();
                        innerResponse.value = messageResponse;

                        try {
                            // Enviar el mensaje
                            callBackPrx.reportResponse(innerResponse);

                            String confirmReceived = "Response was received - Content: " + message + " | sent to: " + k + "\n";

                            try {
                                CallBackPrx callBackPrxInitialSender = ProxiesManager.getInstance().getProxy(hostname);
                                innerResponse.value = confirmReceived;
                                callBackPrxInitialSender.reportResponse(innerResponse);
                            } catch (Exception e) {
                                // Si el remitente inicial no está conectado
                                System.out.println("Error al enviar confirmación al remitente inicial.");
                                e.printStackTrace();

                                PendingResponse pendingResponse = new PendingResponse();
                                pendingResponse.setResponseMessage(confirmReceived);
                                PendingResponseManager.getInstance().addPendingResponse(hostname, pendingResponse);
                            }

                        } catch (Exception e) {
                            // Si el receptor no está conectado
                            System.out.println("Error al enviar mensaje a " + k + " - En: broadcast");
                            e.printStackTrace();

                            innerResponse.value = "Error al enviar mensaje a " + k;

                            // Guardar el mensaje para reintentar
                            PendingResponseSequential pendingResponse = new PendingResponseSequential();
                            pendingResponse.setResponseMessage(messageResponse);
                            pendingResponse.setInitialSender(hostname);
                            PendingResponseManager.getInstance().addPendingResponse(k, pendingResponse);
                        }
                    }
                });
            } catch (Exception e) {
                // Manejo de excepciones generales
                System.out.println("Error en el proceso de broadcast.");
                e.printStackTrace();
            }
        });

        return response;
    }

    @Override
    public Response listClients(String hostname, Current current) {
        Response response = new Response();
        response.value = "ListClients solicitado. Procesando en segundo plano.";

        executorService.execute(() -> {
            try {
                CallBackPrx callBackPrx = ProxiesManager.getInstance().getProxy(hostname);

                if (callBackPrx != null) {
                    String message = ProxiesManager.getInstance().getAllProxies().toString();

                    try {
                        Response innerResponse = new Response();
                        innerResponse.value = message;
                        callBackPrx.reportResponse(innerResponse);

                        innerResponse.value = "Proceso de listClients enviado.";
                    } catch (Exception e) {
                        // Si el receptor no está conectado
                        System.out.println("Error al enviar lista de clientes, " + hostname + " está desconectado.");
                        e.printStackTrace();

                        Response errorResponse = new Response();
                        errorResponse.value = "Error al enviar mensaje a " + hostname;

                        // Guardar el mensaje para reintentar
                        PendingResponse pendingResponse = new PendingResponseSequential();
                        pendingResponse.setResponseMessage(message);
                        PendingResponseManager.getInstance().addPendingResponse(hostname, pendingResponse);
                    }
                }
            } catch (Exception e) {
                // Manejo de excepciones generales
                System.out.println("Error en el proceso de listClients.");
                e.printStackTrace();
            }
        });

        return response;
    }

    @Override
    public Response registerCallback(String hostname, CallBackPrx callBack, Current current) {
        Response response = new Response();
        response.value = "RegisterCallback solicitado. Procesando en segundo plano.";

        executorService.execute(() -> {
            try {
                ProxiesManager.getInstance().addProxy(hostname, callBack);

                System.out.println("Total de proxies registrados:");
                ProxiesManager.getInstance().getAllProxies().forEach((k, v) -> {
                    System.out.println("Hostname: " + k + " | Proxy: " + v);
                });

                // Verificar si hay respuestas pendientes para el cliente
                Queue<PendingResponse> pendingResponses = PendingResponseManager.getInstance().getPendingResponses(hostname);
                if (pendingResponses != null && !pendingResponses.isEmpty()) {
                    // Ejecutar el trabajo de actualización en el thread pool
                    UpdateMessagesJob updateMessagesJob = new UpdateMessagesJob(pendingResponses, callBack);
                    executorService.execute(updateMessagesJob);
                }
            } catch (Exception e) {
                // Manejo de excepciones generales
                System.out.println("Error en el proceso de registerCallback.");
                e.printStackTrace();
            }
        });

        return response;
    }

    @Override
    public Response sendOneMessage(String hostname, String receiver, String message, Current current) {
        Response response = new Response();
        response.value = "SendOneMessage solicitado. Procesando en segundo plano.";

        executorService.execute(() -> {
            try {
                CallBackPrx callBackPrx = ProxiesManager.getInstance().getProxy(receiver);

                if (callBackPrx != null) {
                    String messageResponse = "Message from: " + hostname + " | Message: " + message;
                    Response innerResponse = new Response();
                    innerResponse.value = messageResponse;

                    try {
                        // Enviar el mensaje
                        callBackPrx.reportResponse(innerResponse);

                        String confirmReceived = "Response was received - Content: " + message + " | sent to: " + receiver + "\n";

                        try {
                            // Reportar la respuesta al remitente inicial
                            CallBackPrx callBackPrxInitialSender = ProxiesManager.getInstance().getProxy(hostname);
                            innerResponse.value = confirmReceived;
                            callBackPrxInitialSender.reportResponse(innerResponse);
                        } catch (Exception e) {
                            // Si el remitente inicial no está conectado
                            System.out.println("Error al enviar confirmación al remitente inicial.");
                            e.printStackTrace();

                            PendingResponse pendingResponse = new PendingResponse();
                            pendingResponse.setResponseMessage(confirmReceived);
                            PendingResponseManager.getInstance().addPendingResponse(hostname, pendingResponse);

                            // Actualizar la respuesta si es necesario
                        }

                    } catch (Exception e) {
                        // Si el receptor no está conectado
                        System.out.println("Error al enviar mensaje a " + receiver + " - En: sendOneMessage");
                        e.printStackTrace();

                        Response errorResponse = new Response();
                        errorResponse.value = "Error al enviar mensaje a " + receiver;

                        // Guardar el mensaje para reintentar
                        PendingResponseSequential pendingResponse = new PendingResponseSequential();
                        pendingResponse.setResponseMessage(messageResponse);
                        pendingResponse.setInitialSender(hostname);
                        PendingResponseManager.getInstance().addPendingResponse(receiver, pendingResponse);
                    }
                }
            } catch (Exception e) {
                // Manejo de excepciones generales
                System.out.println("Error en el proceso de sendOneMessage.");
                e.printStackTrace();
            }
        });

        return response;
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
