package com.rxlog.register.domain;
import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(name="publishers")
public class Publisher { @Id @GeneratedValue private UUID id; @Column(nullable=false, unique=true) private String name;
public UUID getId(){return id;} public void setId(UUID id){this.id=id;}
public String getName(){return name;} public void setName(String n){this.name=n;} }
