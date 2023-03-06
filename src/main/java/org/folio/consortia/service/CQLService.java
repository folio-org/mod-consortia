package org.folio.consortia.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaQuery;
import lombok.extern.log4j.Log4j2;
import org.folio.cql2pgjson.exception.QueryValidationException;
import org.folio.consortia.repository.criteria.CQL2JPACriteria;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class CQLService {
  @PersistenceContext
  private EntityManager entityManager;

  public <E> List<E> getByCQL(Class<E> entityCls, String cql, int offset, int limit) {
    try {
      final CQL2JPACriteria<E> cql2JPACriteria = new CQL2JPACriteria<>(entityCls, entityManager);
      final CriteriaQuery<E> criteria = cql2JPACriteria.toCriteria(cql);
      return entityManager
          .createQuery(criteria)
          .setFirstResult(offset)
          .setMaxResults(limit)
          .getResultList();
    } catch (QueryValidationException e) {
      log.error("Can not invoke CQL query {} ", cql);
      throw new IllegalArgumentException(e);
    }
  }

  public <E> Integer countByCQL(Class<E> entityCls, String cql) {
    try {
      final CQL2JPACriteria<E> cql2JPACriteria = new CQL2JPACriteria<>(entityCls, entityManager);
      final CriteriaQuery<E> criteria = cql2JPACriteria.toCriteria(cql);
      return entityManager.createQuery(criteria).getResultList().size();
    } catch (QueryValidationException e) {
      log.error("Can not invoke CQL query {} ", cql);
      throw new IllegalArgumentException(e);
    }
  }
}
