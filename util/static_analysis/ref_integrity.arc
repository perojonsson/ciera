.// This archetype performs static analysis on the activities to check for
.// potential issues with referential integrity.
.//
.// For each create statement, it checks that:
.//   1. All identifier attributes for the instance are initialized before the
.//      end of the body.
.//   2. The instance is not used in a relate statement for a formalized
.//      association in which it is the participant before all its identifying
.//      attributes are set (in the formalizing identifier).
.//   3. None of the identifier attributes are reassigned by assignment
.//      statement.
.//
.// For each assignment of an identifying attribute (regular assignment or
.// relate statement for referential attribute as identifier), it checks that:
.//   1. There is a create statement earlier in the body
.//
.// The algorithm considers all statements within "while" and "for" blocks
.// potentially unreachable. Statements within "if", "elif", and "else" blocks
.// are considered potentially unreachable, however, if an identifying attribute
.// is set in all blocks of an "if" statement, it is considered to be
.// initialized. Any statement after a "return" statement is considered
.// potentially unreachable.
.//
.// The algorithm considers assignment statements and relate statements for
.// assigning identifier attributes. It does not consider assignments as part
.// of expressions.
.//
.// For relate statements that initialize referential attributes as identifier,
.// reassignments are not flagged -- it is valid to unrelate and re-relate
.// instances in this situation.
.//
.function emit_warning
  .param inst_ref smt
  .param string msg
  .select one act related by smt->ACT_BLK[R602]->ACT_ACT[R601]
  .print "Warning: ${act.Label} line: ${smt.LineNumber} -- ${msg}"
.end function
.//
.function is_fully_initialized_in_block
  .param inst_ref var
  .param inst_ref block
  .assign attr_ret = true
  .select one obj related by var->S_DT[R848]->S_IRDT[R17]->O_OBJ[R123]
  .if ( empty obj )
    .select one obj related by var->V_INT[R814]->O_OBJ[R818]
  .end if
  .select many oidas related by obj->O_ID[R104]->O_OIDA[R105]
  .select many oidis related by oidas->O_OIDI[R199] where ( ( selected.Var_ID == var.Var_ID ) and ( selected.Block_ID == block.Block_ID ) )
  .assign card_oidis = cardinality oidis
  .assign attr_ret = ( ( attr_ret ) and ( ( cardinality oidas ) == ( cardinality oidis ) ) )
  .assign oida_card = cardinality oidas
  .assign oidi_card = cardinality oidis
  .for each oidi in oidis
    .assign attr_ret = ( ( attr_ret ) and ( oidi.initialized ) )
  .end for
.end function
.//
.function is_initialized_in_if
  .param inst_ref var
  .param inst_ref oida
  .param inst_ref if_smt
  .assign attr_ret = true
  .select one if_blk related by if_smt->ACT_BLK[R607]
  .select many elif_blks related by if_smt->ACT_EL[R682]->ACT_BLK[R658]
  .select one else_blk related by if_smt->ACT_E[R683]->ACT_BLK[R606]
  .// check "if" block
  .select any oidi related by oida->O_OIDI[R199] where ( ( selected.Var_ID == var.Var_ID ) and ( selected.Block_ID == if_blk.Block_ID ) )
  .assign attr_ret = ( ( attr_ret ) and ( oidi.initialized ) )
  .// check "elif" blocks
  .for each elif_blk in elif_blks
    .select any oidi related by oida->O_OIDI[R199] where ( ( selected.Var_ID == var.Var_ID ) and ( selected.Block_ID == elif_blk.Block_ID ) )
    .if ( not_empty oidi )
      .assign attr_ret = ( ( attr_ret ) and ( oidi.initialized ) )
    .end if
  .end for
  .// check "else" block
  .if ( not_empty else_blk )
    .select any oidi related by oida->O_OIDI[R199] where ( ( selected.Var_ID == var.Var_ID ) and ( selected.Block_ID == else_blk.Block_ID ) )
    .if ( not_empty oidi )
      .assign attr_ret = ( ( attr_ret ) and ( oidi.initialized ) )
    .end if
  .end if
.end function
.//
.function get_unique_id_dt_id
  .select any dt from instances of S_DT where ( selected.Name == "unique_id" )
  .assign attr_dt_id = dt.DT_ID
.end function
.//
.function descend_block
  .param inst_ref smt
  .param boolean in_for_while
  .param inst_ref_set oidas
  .param inst_ref var
  .select any attr_block from instances of ACT_BLK where ( false )
  .select any attr_smt from instances of ACT_SMT where ( false )
  .assign attr_in_for_while = in_for_while
  .invoke result = get_unique_id_dt_id()
  .assign unique_id = result.dt_id
  .select one if_smt related by smt->ACT_IF[R603]
  .select one elif_smt related by smt->ACT_EL[R603]
  .select one else_smt related by smt->ACT_E[R603]
  .select one for_smt related by smt->ACT_FOR[R603]
  .select one while_smt related by smt->ACT_WHL[R603]
  .if ( ( ( ( not_empty if_smt ) or ( not_empty elif_smt ) ) or ( ( not_empty else_smt ) or ( not_empty for_smt ) ) ) or ( not_empty while_smt ) )
    .if ( not_empty if_smt )
      .select one attr_block related by if_smt->ACT_BLK[R607]
    .end if
    .if ( not_empty elif_smt )
      .select one attr_block related by elif_smt->ACT_BLK[R658]
    .end if
    .if ( not_empty else_smt )
      .select one attr_block related by else_smt->ACT_BLK[R606]
    .end if
    .if ( not_empty for_smt )
      .select one attr_block related by for_smt->ACT_BLK[R605]
      .assign attr_in_for_while = true
    .end if
    .if ( not_empty while_smt )
      .select one attr_block related by while_smt->ACT_BLK[R608]
      .assign attr_in_for_while = true
    .end if
    .// create initializations for the new block
    .for each oida in oidas
      .// set each oida uninitialized
      .// unique_id attrs are automatically initialized
      .select one attr related by oida->O_ATTR[R105]
      .create object instance oidi of O_OIDI
      .assign oidi.initialized = ( attr.DT_ID == unique_id )
      .relate oidi to var across R199
      .relate oidi to oida across R199
      .relate oidi to attr_block across R198
    .end for
    .// select first statement in inner block
    .select any attr_smt related by attr_block->ACT_SMT[R602]
    .select one prev_smt related by attr_smt->ACT_SMT[R661.'succeeds']
    .while ( not_empty prev_smt )
      .assign attr_smt = prev_smt
      .select one prev_smt related by attr_smt->ACT_SMT[R661.'succeeds']
    .end while
  .end if
.end function
.//
.function ascend_block
  .param inst_ref block
  .param boolean in_for_while
  .select any attr_block from instances of ACT_BLK where ( false )
  .select any attr_smt from instances of ACT_SMT where ( false )
  .assign attr_in_for_while = in_for_while
  .select one if_smt related by block->ACT_IF[R607]
  .select one elif_smt related by block->ACT_EL[R658]
  .select one else_smt related by block->ACT_E[R606]
  .select one for_smt related by block->ACT_FOR[R605]
  .select one while_smt related by block->ACT_WHL[R608]
  .if ( ( ( ( not_empty if_smt ) or ( not_empty elif_smt ) ) or ( ( not_empty else_smt ) or ( not_empty for_smt ) ) ) or ( not_empty while_smt ) )
    .if ( not_empty if_smt )
      .// select the "elif" or "else" if there is one
      .select many next_elif_smts related by if_smt->ACT_EL[R682]->ACT_SMT[R603]
      .if ( not_empty next_elif_smts )
        .select any smallest_smt related by if_smt->ACT_EL[R682]->ACT_SMT[R603]
        .for each next_elif_smt in next_elif_smts
          .if ( next_elif_smt.LineNumber < smallest_smt.LineNumber )
            .assign smallset_smt = next_elif_smt
          .end if
        .end for
        .assign attr_smt = smallest_smt
        .select one attr_block related by attr_smt->ACT_BLK[R602]
      .else
        .select one next_else_smt related by if_smt->ACT_E[R683]
        .if ( not_empty next_else_smt )
          .select one attr_smt related by next_else_smt->ACT_SMT[R603]
          .select one attr_block related by attr_smt->ACT_BLK[R602]
        .else
          .// no elif or else
          .select one smt related by if_smt->ACT_SMT[R603]
          .select one attr_block related by smt->ACT_BLK[R602]
          .select one attr_smt related by smt->ACT_SMT[R661.'precedes']
        .end if
      .end if
    .end if
    .if ( not_empty elif_smt )
      .// select the next "elif" or "else" if there is one
      .select one current_smt related by elif_smt->ACT_SMT[R603]
      .select many next_elif_smts related by elif_smt->ACT_IF[R682]->ACT_EL[R682]->ACT_SMT[R603] where ( selected.LineNumber > current_smt.LineNumber )
      .if ( not_empty next_elif_smts )
        .select any smallest_smt related by elif_smt->ACT_IF[R682]->ACT_EL[R682]->ACT_SMT[R603] where ( selected.LineNumber > current_smt.LineNumber )
        .for each next_elif_smt in next_elif_smts
          .if ( next_elif_smt.LineNumber < smallest_smt.LineNumber )
            .assign smallset_smt = next_elif_smt
          .end if
        .end for
        .assign attr_smt = smallest_smt
        .select one attr_block related by attr_smt->ACT_BLK[R602]
      .else
        .select one next_else_smt related by elif_smt->ACT_IF[R682]->ACT_E[R683]
        .if ( not_empty next_else_smt )
          .select one attr_smt related by next_else_smt->ACT_SMT[R603]
          .select one attr_block related by attr_smt->ACT_BLK[R602]
        .else
          .// no elif or else
          .select one smt related by elif_smt->ACT_IF[R682]->ACT_SMT[R603]
          .select one attr_block related by smt->ACT_BLK[R602]
          .select one attr_smt related by smt->ACT_SMT[R661.'precedes']
        .end if
      .end if
    .end if
    .if ( not_empty else_smt )
      .select one smt related by else_smt->ACT_IF[R683]->ACT_SMT[R603]
      .select one attr_block related by smt->ACT_BLK[R602]
      .select one attr_smt related by smt->ACT_SMT[R661.'precedes']
    .end if
    .if ( not_empty for_smt )
      .select one smt related by for_smt->ACT_SMT[R603]
      .assign attr_in_for_while = false
      .select one attr_block related by smt->ACT_BLK[R602]
      .select one attr_smt related by smt->ACT_SMT[R661.'precedes']
    .end if
    .if ( not_empty while_smt )
      .select one smt related by while_smt->ACT_SMT[R603]
      .assign attr_in_for_while = false
      .select one attr_block related by smt->ACT_BLK[R602]
      .select one attr_smt related by smt->ACT_SMT[R661.'precedes']
    .end if
  .end if
.end function
.//
.function check_creates
  .select many bodies from instances of ACT_ACT
  .invoke result = get_unique_id_dt_id()
  .assign unique_id = result.dt_id
  .for each body in bodies
    .// get all create no variables. For these statements, they are invalide
    .// unless they have no identifying attributes
    .select many create_nv_smts related by body->ACT_BLK[R601]->ACT_SMT[R602]->ACT_CNV[R603]
    .for each create_nv_smt in create_nv_smts
      .select any id_attr related by create_nv_smt->O_OBJ[R672]->O_ID[R104]->O_OIDA[R105]->O_ATTR[R105] where ( selected.DT_ID != unique_id )
      .if ( not_empty id_attr )
        .select one smt related by create_nv_smt->ACT_SMT[R603]
        .select one obj related by create_nv_smt->O_OBJ[R672]
        .invoke emit_warning( smt, "Create no variable statement used with class that has identifying attributes: ${obj.Key_Lett}" )
      .end if
    .end for
    .// get all create statements
    .select many create_smts related by body->ACT_BLK[R601]->ACT_SMT[R602]->ACT_CR[R603]
    .for each create_smt in create_smts
      .select one smt related by create_smt->ACT_SMT[R603]
      .select one obj related by create_smt->O_OBJ[R671]
      .select one var related by create_smt->V_VAR[R633]
      .select one block related by smt->ACT_BLK[R602]
      .select many oidas related by obj->O_ID[R104]->O_OIDA[R105]
      .for each oida in oidas
        .// set each oida uninitialized
        .// unique_id attrs are automatically initialized
        .select one attr related by oida->O_ATTR[R105]
        .create object instance oidi of O_OIDI
        .assign oidi.initialized = ( attr.DT_ID == unique_id )
        .relate oidi to var across R199
        .relate oidi to oida across R199
        .relate oidi to block across R198
      .end for
      .// iterate through all following statements
      .select one smt related by smt->ACT_SMT[R661.'precedes']
      .assign done = false
      .assign original_block = block
      .assign in_for_while = false
      .while ( ( not_empty block ) and ( not done ) )
        .while ( ( not_empty smt ) and ( not done ) )
          .//.print "Processing statement on line: ${smt.LineNumber}"
          .// check assignment statement initializations
          .select one assign_smt related by smt->ACT_AI[R603]
          .if ( not_empty assign_smt )
            .select one assigned_to_oattr related by assign_smt->V_VAL[R689]->V_AVL[R801]->O_ATTR[R806]
            .select one assigned_to_obj related by assigned_to_oattr->O_OBJ[R102]
            .if ( assigned_to_obj == obj )
              .select many assigned_to_oidas related by assigned_to_oattr->O_OIDA[R105]
              .for each assigned_to_oida in assigned_to_oidas
                .select any oidi related by assigned_to_oida->O_OIDI[R199] where ( ( selected.Var_ID == var.Var_ID ) and ( selected.Block_ID == block.Block_ID ) )
                .if ( oidi.initialized )
                  .invoke emit_warning( smt, "Reassignment of identifying attribute: ${var.Name}.${assigned_to_oattr.Name}" )
                .else
                  .if ( not in_for_while )
                    .assign oidi.initialized = true
                  .end if
                .end if
              .end for
            .end if
          .end if
          .// check relate statement initializations
          .select many ref_idas related by obj->O_ATTR[R102]->O_RATTR[R106]->O_ATTR[R106]->O_OIDA[R105]
          .if ( not_empty ref_idas )
            .select one relate_smt related by smt->ACT_REL[R603]
            .select one relate_using_smt related by smt->ACT_RU[R603]
            .if ( ( not_empty relate_smt ) or ( not_empty relate_using_smt ) )
              .select one rel related by relate_using_smt->R_REL[R654]
              .if ( empty rel )
                .select one rel related by relate_smt->R_REL[R653]
              .end if
              .select any rgo related by rel->R_OIR[R201]->R_RGO[R203] where ( selected.Obj_ID == obj.Obj_ID )
              .if ( not_empty rgo )
                .select one one_var related by relate_using_smt->V_VAR[R617]
                .select one oth_var related by relate_using_smt->V_VAR[R618]
                .select one use_var related by relate_using_smt->V_VAR[R619]
                .if ( ( empty one_var ) and ( empty oth_var ) )
                  .select one one_var related by relate_smt->V_VAR[R615]
                  .select one oth_var related by relate_smt->V_VAR[R616]
                .end if
                .if ( ( ( var == one_var ) or ( var == oth_var ) ) or ( var == use_var ) )
                  .for each ref_ida in ref_idas
                    .select any oidi related by ref_ida->O_OIDI[R199] where ( ( selected.Var_ID == var.Var_ID ) and ( selected.Block_ID == block.Block_ID ) )
                    .if ( not in_for_while )
                      .assign oidi.initialized = true
                    .end if
                  .end for
                .end if
              .end if
            .end if
          .end if
          .// check if all are initialized (short circuit)
          .//.invoke result = is_fully_initialized_in_block( var, block )
          .//.assign done = result.ret
          .// select inner block for "if", "for", and "while" statements
          .invoke result = descend_block( smt, in_for_while, oidas, var )
          .if ( not_empty result.block )
            .assign block = result.block
            .assign smt = result.smt
            .assign in_for_while = result.in_for_while
          .// select next statement
          .else
            .select one smt related by smt->ACT_SMT[R661.'precedes']
          .end if
        .end while
        .// select outer block and next statment
        .if ( block != original_block )
          .// for "if", "elif", and "else" statements, if the initialization
          .// is in every block, it is in the outer block
          .select one if_smt related by block->ACT_IF[R607]
          .if ( empty if_smt )
            .select one if_smt related by block->ACT_EL[R658]->ACT_IF[R682]
            .if ( empty if_smt )
              .select one if_smt related by block->ACT_E[R606]->ACT_IF[R683]
            .end if
          .end if
          .if ( not_empty if_smt )
            .for each oida in oidas
              .select one outer_block related by if_smt->ACT_SMT[R603]->ACT_BLK[R602]
              .invoke result = is_initialized_in_if( var, oida, if_smt )
              .select any oidi related by oida->O_OIDI[R199] where ( ( selected.Var_ID == var.Var_ID ) and ( selected.Block_ID == outer_block.Block_ID ) )
              .assign oidi.initialized = result.ret
            .end for
          .end if
          .// ascend to the outer block or go to the next block (for if and elif statements)
          .invoke result = ascend_block( block, in_for_while )
          .assign block = result.block
          .assign smt = result.smt
          .assign in_for_while = result.in_for_while
        .else
          .assign done = true
        .end if
      .end while
      .// check if the identifiers are potentially uninitialized
      .invoke result = is_fully_initialized_in_block( var, original_block )
      .if ( not result.ret )
        .select one smt related by create_smt->ACT_SMT[R603]
        .invoke emit_warning( smt, "Instance identifiers may not have been initialized after create statement: ${obj.Key_Lett}" )
      .end if
    .end for .// for each create_smt in create_smts
  .end for .// for each body in bodies
.end function
.//
.function check_assignments
.end function
.//
.function analyze
  .invoke check_creates()
  .invoke check_assignments()
.end function
.//
.//------ MAIN CODE -------//
.invoke analyze()
