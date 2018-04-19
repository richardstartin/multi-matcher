package com.openkappa.bitrules;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class TestDomainObject {

  public static TestDomainObject random() {
    return new TestDomainObject(UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            ThreadLocalRandom.current().nextDouble(),
            ThreadLocalRandom.current().nextInt(),
            ThreadLocalRandom.current().nextLong()
    );
  }

  private String field1;
  private String field2;
  private String field3;
  private String field4;
  private String field5;
  private double measure1;
  private int measure2;
  private long measure3;


  public TestDomainObject(String field1,
                          String field2,
                          String field3,
                          String field4,
                          String field5,
                          double measure1,
                          int measure2,
                          long measure3) {
    this.field1 = field1;
    this.field2 = field2;
    this.field3 = field3;
    this.field4 = field4;
    this.field5 = field5;
    this.measure1 = measure1;
    this.measure2 = measure2;
    this.measure3 = measure3;
  }


  public String getField1() {
    return field1;
  }

  public String getField2() {
    return field2;
  }

  public String getField3() {
    return field3;
  }

  public String getField4() {
    return field4;
  }

  public String getField5() {
    return field5;
  }

  public double getMeasure1() {
    return measure1;
  }

  public int getMeasure2() {
    return measure2;
  }

  public long getMeasure3() {
    return measure3;
  }

  public TestDomainObject setField1(String field1) {
    this.field1 = field1;
    return this;
  }

  public TestDomainObject setField2(String field2) {
    this.field2 = field2;
    return this;
  }

  public TestDomainObject setField3(String field3) {
    this.field3 = field3;
    return this;
  }

  public TestDomainObject setField4(String field4) {
    this.field4 = field4;
    return this;
  }

  public TestDomainObject setField5(String field5) {
    this.field5 = field5;
    return this;
  }

  public TestDomainObject setMeasure1(double measure1) {
    this.measure1 = measure1;
    return this;
  }

  public TestDomainObject setMeasure2(int measure2) {
    this.measure2 = measure2;
    return this;
  }

  public TestDomainObject setMeasure3(long measure3) {
    this.measure3 = measure3;
    return this;
  }
}
