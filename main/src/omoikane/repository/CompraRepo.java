package omoikane.repository;

import omoikane.compras.entities.Compra;
import org.synyx.hades.dao.GenericDao;

/**
 * Created with IntelliJ IDEA.
 * User: octavioruizcastillo
 * Date: 23/11/13
 * Time: 20:40
 * To change this template use File | Settings | File Templates.
 */
public interface CompraRepo extends GenericDao<Compra, Long> {
    Compra findByCompletado(Boolean completado);
}
