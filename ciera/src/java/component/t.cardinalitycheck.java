T_b("        ");
T_b("if ( !((");
T_b(self->relationship_set_cast);
T_b(")getRelationshipSet( ");
T_b(T_s(rel_num));
T_b(" ) ).getBy");
T_b(T_c(self->participant));
T_b("Id( ");
T_b(self->link_parameter_name);
T_b(".getInstanceId() ).isEmpty() ) throw new ModelIntegrityException( \"Cannot relate more than one instance across association.\" );");
T_b("\n");
