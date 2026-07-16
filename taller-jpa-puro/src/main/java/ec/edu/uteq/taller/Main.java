package ec.edu.uteq.taller;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        try (AnnotationConfigApplicationContext ctx =
                     new AnnotationConfigApplicationContext(JpaConfig.class)) {

            ProductoRepository repo = ctx.getBean(ProductoRepository.class);

            // ------------------------------------------------------------
            // 1) Medicion del listado con System.nanoTime()
            // ------------------------------------------------------------
            long inicio = System.nanoTime();
            List<Producto> lista1 = repo.findAll();
            long fin = System.nanoTime();
            double ms1 = (fin - inicio) / 1_000_000.0;
            System.out.printf("nanoTime : %d filas en %.3f ms %n",
                    lista1.size(), ms1);

            // ------------------------------------------------------------
            // 2) Medicion del listado con StopWatch
            // ------------------------------------------------------------
            StopWatch sw = new StopWatch("listar-jpa");
            sw.start("findAll()");
            List<Producto> lista2 = repo.findAll();
            sw.stop();
            System.out.printf("StopWatch: %d filas en %.3f ms %n",
                    lista2.size(),
                    sw.getTotalTimeNanos() / 1_000_000.0);
            System.out.println(sw.prettyPrint());

            // ------------------------------------------------------------
            // 3) Crear un producto nuevo
            // ------------------------------------------------------------
            Producto nuevo = new Producto(
                    "Producto de prueba JPA", new BigDecimal("99.99"), 5);
            Producto guardado = repo.save(nuevo);
            System.out.println("Creado con id = " + guardado.getId());

            // ------------------------------------------------------------
            // 4) Eliminarlo para dejar la base como estaba
            // ------------------------------------------------------------
            repo.deleteById(guardado.getId());
            System.out.println("Eliminado correctamente");
        }
    }
}