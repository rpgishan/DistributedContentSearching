package mrt.cse.msc.dc.cybertronez;

public class Query
{
  private String query;
  private Node initiatedNode;

  public Query(final String query, final Node initiatedNode)
  {
    this.query = query;
    this.initiatedNode = initiatedNode;
  }

  public String getQuery()
  {
    return query;
  }

  public Node getInitiatedNode()
  {
    return initiatedNode;
  }

  @Override
  public String toString()
  {
    return "Query{" +
        "query='" + query + '\'' +
        ", initiatedNode=" + initiatedNode +
        '}';
  }
}
