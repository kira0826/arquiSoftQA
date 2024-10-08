package commands;

import java.util.ArrayList;
import java.util.List;

import Demo.Response;

public class FibonacciAndPrimesCommand implements Command{

    @Override
    public Response execute(String username, String hostname, String[] args) {

        Response response = new Response(); 

        int n = Integer.parseInt(args[0]);  
                
        // Imprimir la serie de Fibonacci en la consola

        String fibonacciSeries = generateFibonacci(n);
        
        System.out.println(username + "/" + hostname + " - Fibonacci series for " + n + ": " + fibonacciSeries);

        // Calcular los factores primos de n y devolverlos como respuesta

        List<Integer> primeFactors = getPrimeFactors(n);
        String factores = "Prime factors of " + n + ": " + primeFactors.toString() + "/n";
        System.out.println(factores);
        
        response.value = "Factors and fibonacci printed on server.";
        
        return response;

    }
    

    private String generateFibonacci(int n) {
        StringBuilder fibonacciSeries = new StringBuilder();
        int a = 0, b = 1;

        if (n >= 1) fibonacciSeries.append(a).append(" ");
        if (n >= 2) fibonacciSeries.append(b).append(" ");

        for (int i = 3; i <= n; i++) {
            int next = a + b;
            fibonacciSeries.append(next).append(" ");
            a = b;
            b = next;
        }

        return fibonacciSeries.toString();
    }


       // Método para obtener los factores primos de un número
    private List<Integer> getPrimeFactors(int n) {
        List<Integer> primeFactors = new ArrayList<>();

        // Dividir n por 2 mientras sea divisible
        while (n % 2 == 0) {
            primeFactors.add(2);
            n /= 2;
        }

        // Dividir n por números impares
        for (int i = 3; i <= Math.sqrt(n); i += 2) {
            while (n % i == 0) {
                primeFactors.add(i);
                n /= i;
            }
        }

        // Si n es un número primo mayor que 2
        if (n > 2) {
            primeFactors.add(n);
        }

        return primeFactors;
    }
   
}
