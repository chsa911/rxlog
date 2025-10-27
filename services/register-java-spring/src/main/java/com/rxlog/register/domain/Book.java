package com.rxlog.register.domain;
import jakarta.persistence.*; import java.util.*; 
@Entity @Table(name="books")
public class Book {
  @Id @GeneratedValue private UUID id;
  @Column(nullable=false) private String title;
  @ManyToOne(optional=false) @JoinColumn(name="author_id") private Author author;
  @ManyToOne(optional=false) @JoinColumn(name="publisher_id") private Publisher publisher;
  @ElementCollection @CollectionTable(name="book_barcodes", joinColumns=@JoinColumn(name="book_id"))
  @Column(name="barcode") private List<String> barcodes = new ArrayList<>();
  public UUID getId(){return id;} public void setId(UUID id){this.id=id;}
  public String getTitle(){return title;} public void setTitle(String t){this.title=t;}
  public Author getAuthor(){return author;} public void setAuthor(Author a){this.author=a;}
  public Publisher getPublisher(){return publisher;} public void setPublisher(Publisher p){this.publisher=p;}
  public List<String> getBarcodes(){return barcodes;} public void setBarcodes(List<String> b){this.barcodes=b;}
}
