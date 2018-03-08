.if ( is_getter )
    public ${type_name} get${capital_name}() throws XtumlException {
        checkLiving();
        if ( this instanceof IEmptyInstance ) throw new EmptyInstanceException( "Cannot get attribute of empty instance." );
${attribute_derivation}
        return ${self.attribute_name};
    }
.else
    public void set${capital_name}( ${type_name} ${self.attribute_name} ) throws XtumlException {
        checkLiving();
        if ( this instanceof IEmptyInstance ) throw new EmptyInstanceException( "Cannot set attribute of empty instance." );
  .if ( primitive )
        if ( ${self.attribute_name} != this.${self.attribute_name} ) {
  .else
        if ( !${self.attribute_name}.equals( this.${self.attribute_name} ) ) {
  .end if
            this.${self.attribute_name} = ${self.attribute_name};
${attribute_propagations}\
        }
    }
.end if