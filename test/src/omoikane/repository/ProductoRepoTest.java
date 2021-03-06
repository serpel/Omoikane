package omoikane.repository;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import omoikane.producto.Articulo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.synyx.hades.domain.PageRequest;
import org.synyx.hades.domain.Pageable;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: octavioruizcastillo
 * Date: 30/10/12
 * Time: 18:16
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class })
@DatabaseSetup("sampleData.xml")
public class ProductoRepoTest {
    @Autowired
    ProductoRepo productoRepo;

    @Test
    public void testFindByDescripcion() {
        Pageable pagina = new PageRequest(0, 10);
        ArrayList<Articulo> productos = (ArrayList<Articulo>) productoRepo.findByDescripcionLike("%" + "CONCEPCION" + "%", pagina);
        for(Articulo p : productos) {
            System.out.println("-" + p.getDescripcion());
        }
    }

    @Test
    public void testFindByCodigo() {
        ArrayList<Articulo> productos = (ArrayList<Articulo>) productoRepo.findByCodigo("7501059238305");
        for(Articulo a : productos) {
            System.out.println("-" + a.getDescripcion());
        }
    }
}
