if ( is_getter ) {
T_b("    ");
T_b("@Override");
T_b("\n");
T_b("    ");
T_b("public ");
T_b(type_name);
T_b(" ");
T_b(name);
T_b("() throws XtumlException {");
T_b("\n");
T_b("        ");
T_b("checkLiving();");
T_b("\n");
T_b(attribute_derivation);
T_b("        return ");
T_b(self->attribute_name);
T_b(";");
T_b("\n");
T_b("    ");
T_b("}");
T_b("\n");
} else {
T_b("    ");
T_b("@Override");
T_b("\n");
T_b("    ");
T_b("public void ");
T_b(name);
T_b("( ");
T_b(type_name);
T_b(" ");
T_b(self->attribute_name);
T_b(" ) throws XtumlException {");
T_b("\n");
T_b("        ");
T_b("checkLiving();");
T_b("\n");
if ( primitive ) {
T_b("        ");
T_b("if ( ");
T_b(self->attribute_name);
T_b(" != this.");
T_b(self->attribute_name);
T_b(" ) {");
T_b("\n");
} else {
T_b("        ");
T_b("if ( !");
T_b(self->attribute_name);
T_b(".equality( this.");
T_b(self->attribute_name);
T_b(" ) ) {");
T_b("\n");
}
T_b("            ");
T_b("this.");
T_b(self->attribute_name);
T_b(" = ");
T_b(self->attribute_name);
T_b(";");
T_b("\n");
T_b(propagations);
T_b("        }");
T_b("\n");
T_b("    ");
T_b("}");
T_b("\n");
}
