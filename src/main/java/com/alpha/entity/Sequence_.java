package com.alpha.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2018-04-15T21:28:49.883+0900")
@StaticMetamodel(Sequence.class)
public class Sequence_ {
	public static volatile SingularAttribute<Sequence, String> name;
	public static volatile SingularAttribute<Sequence, Integer> currentValue;
	public static volatile SingularAttribute<Sequence, Integer> increment;
}
