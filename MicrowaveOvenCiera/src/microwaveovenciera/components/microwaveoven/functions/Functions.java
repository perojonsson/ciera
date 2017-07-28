package microwaveovenciera.components.microwaveoven.functions;

import ciera.exceptions.XtumlException;
import microwaveovenciera.components.microwaveoven.MicrowaveOven;
import microwaveovenciera.components.microwaveoven.datatypes.TubeWattage;
import microwaveovenciera.components.microwaveoven.microwaveoven.*;
import microwaveovenciera.components.microwaveoven.microwaveoven.door.instancestatemachine.Close;
import microwaveovenciera.components.microwaveoven.microwaveoven.door.instancestatemachine.Release;
import microwaveovenciera.components.microwaveoven.microwaveoven.oven.instancestatemachine.CancelCooking;
import microwaveovenciera.components.microwaveoven.microwaveoven.oven.instancestatemachine.CookingPeriod;
import microwaveovenciera.components.microwaveoven.microwaveoven.oven.instancestatemachine.StartCooking;
import microwaveovenciera.components.microwaveoven.microwaveoven.magnetrontube.instancestatemachine.DecreasePower;
import microwaveovenciera.components.microwaveoven.microwaveoven.magnetrontube.instancestatemachine.IncreasePower;
import microwaveovenciera.components.microwaveoven.testsubsystem.TestSequences;
import microwaveovenciera.components.microwaveoven.testsubsystem.testsequences.instancestatemachine.PerformTestSeq1;

public class Functions {
    
    public static void CancelCooking( MicrowaveOven context ) throws XtumlException {
        // select any oven from instances of MO_O;
        Oven oven = context.selectAnyMO_OFromInstances();
        // generate MO_O4:'cancel_cooking'  to oven;
        new CancelCooking().generateTo( oven );
    }

    public static void CloseDoor( MicrowaveOven context ) throws XtumlException {
        // select any door from instances of MO_D;
        Door door = context.selectAnyMO_DFromInstances();
        // generate MO_D2:'close'  to door;
        new Close().generateTo( door );
    }
    
    public static void DecreasePower( MicrowaveOven context ) throws XtumlException {
        // select any tube from instances of MO_MT;
        MagnetronTube tube = context.selectAnyMO_MTFromInstances();
        // generate MO_MT2:'decrease_power' to tube;
        new DecreasePower().generateTo( tube );
    }
    
    public static void DefineOven( MicrowaveOven context ) throws XtumlException {
        // Create the instances in the system.
        // create object instance mo of MO_O;
        Oven mo = context.createObjectInstance( new Oven() );
        // assign mo.remaining_cooking_time = 0;
        mo.setM_remaining_cooking_time( 0 );
        // create object instance tube of MO_MT;
        MagnetronTube tube = context.createObjectInstance( new MagnetronTube() );
        // relate mo to tube across R1;
        mo.relateToMO_MTAcrossR1( tube );
        // assign tube.current_power_output = tube_wattage::high;
        tube.setM_current_power_output( TubeWattage.HIGH );
        // create object instance light of MO_IL;
        InternalLight light = context.createObjectInstance( new InternalLight() );
        // relate mo to light across R2;
        mo.relateToMO_ILAcrossR2( light );
        // create object instance beeper of MO_B;
        Beeper beeper = context.createObjectInstance( new Beeper() );
        // relate mo to beeper across R3;
        mo.relateToMO_BAcrossR3( beeper );
        // create object instance door of MO_D;
        Door door = context.createObjectInstance( new Door() );
        // relate mo to door across R4;
        mo.relateToMO_DAcrossR4( door );
        // assign door.is_secure = false;
        door.setM_is_secure( false );
        // create object instance turntable of MO_TRN;
        Turntable turntable = context.createObjectInstance( new Turntable() );
        // relate mo to turntable across R5;
        mo.relateToMO_TRNAcrossR5( turntable );
    }
    
    public static void IncreasePower( MicrowaveOven context ) throws XtumlException {
        // select any tube from instances of MO_MT;
        MagnetronTube tube = context.selectAnyMO_MTFromInstances();
        // generate MO_MT1:'increase_power'  to tube;
        new IncreasePower().generateTo( tube );
    }
    
    public static void OpenDoor( MicrowaveOven context ) throws XtumlException {
        // select any door from instances of MO_D;
        Door door = context.selectAnyMO_DFromInstances();
        // generate MO_D1:'release'  to door;
        new Release().generateTo( door );
    }
    
    public static void SpecifyCookingPeriod( MicrowaveOven context, int cookingPeriod ) throws XtumlException {
        // cooking period is to be specified in seconds and must be converted to usec in order
        // to be compatible with BP's view of time
        // timePeriod = 1000000 * param.cookingPeriod;
        int timePeriod = 1000000 * cookingPeriod;
        // select any oven from instances of MO_O;
        Oven oven = context.selectAnyMO_OFromInstances();
        // generate MO_O8:'cooking_period' (period:timePeriod) to oven;
        new CookingPeriod( timePeriod ).generateTo( oven );
    }
    
    public static void StartCooking( MicrowaveOven context ) throws XtumlException {
        // select any oven from instances of MO_O;
        Oven oven = context.selectAnyMO_OFromInstances();
        // generate MO_O3:'start_cooking'  to oven;
        new StartCooking().generateTo( oven );
    }
    
    public static void TestSequence1( MicrowaveOven context ) throws XtumlException {
        // create object instance testSequence of MO_TS;
        TestSequences testSequence = context.createObjectInstance( new TestSequences() );
        // generate MO_TS2 to testSequence;
        new PerformTestSeq1().generateTo( testSequence );
    }

}