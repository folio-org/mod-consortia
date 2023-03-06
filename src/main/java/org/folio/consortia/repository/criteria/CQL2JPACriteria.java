package org.folio.consortia.repository.criteria;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.log4j.Log4j2;
import org.folio.cql2pgjson.exception.CQLFeatureUnsupportedException;
import org.folio.cql2pgjson.exception.QueryValidationException;
import org.folio.cql2pgjson.util.Cql2SqlUtil;
import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLParseException;
import org.z3950.zing.cql.CQLParser;
import org.z3950.zing.cql.CQLTermNode;

import java.io.IOException;

@Log4j2
public class CQL2JPACriteria<E> {

  private final CriteriaBuilder builder;
  public final Root<E> root;
  public static final String NOT_EQUALS_OPERATOR = "<>";
  public final CriteriaQuery<E> criteria;

  public CQL2JPACriteria(Class<E> entityCls, EntityManager entityManager) {
    this.builder = entityManager.getCriteriaBuilder();
    criteria = builder.createQuery(entityCls);
    root = criteria.from(entityCls);
  }

  public CriteriaQuery<E> toCriteria(String cql) throws QueryValidationException {
    try {
      CQLParser parser = new CQLParser();
      CQLNode node = parser.parse(cql);
      return toCriteria(node);
    } catch (IOException | CQLParseException e) {
      throw new QueryValidationException(e);
    }
  }

  private CriteriaQuery<E> toCriteria(CQLNode node)
    throws QueryValidationException {
    Predicate predicates = process(node);
    return criteria.where(predicates);
  }

  private Predicate process(CQLNode node) throws QueryValidationException {
    return processTerm((CQLTermNode) node);
  }

  private Predicate processTerm(CQLTermNode node) throws QueryValidationException {
    String fieldName = node.getIndex();
    var field = getPath(fieldName);
    return indexNode(field, node);
  }

  private Path<?> getPath(String fieldName) {
    return root.get(fieldName);
  }

  private Predicate indexNode(Path<?> field, CQLTermNode node)
      throws QueryValidationException {

    String comparator = node.getRelation().getBase().toLowerCase();

    switch (comparator) {
      case "adj", "all", "any", "==", NOT_EQUALS_OPERATOR:
        return buildQuery(field, node);
      default:
        throw new CQLFeatureUnsupportedException(
            "Relation " + comparator + " not implemented yet: " + node);
    }
  }

  private Predicate buildQuery(Path<?> field, CQLTermNode node) {
    return queryByLike((Path<String>) field, node);
  }

  /** Create an SQL expression using LIKE query syntax. */
  private Predicate queryByLike(Path<String> field, CQLTermNode node) {
    return builder.like(field, Cql2SqlUtil.cql2like(node.getTerm()));
  }

}
