package LifeValuable.Library.repository;

public interface BookLendingProjection {
    Long getId();
    String getTitle();
    String getAuthor();
    Integer getLendingCount();
}
