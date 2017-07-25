package microwaveovenciera.components.microwaveoven.microwaveoven.magnetrontube.instancestatemachine;

import microwaveovenciera.components.microwaveoven.datatypes.TubeWattage;
import microwaveovenciera.components.microwaveoven.microwaveoven.MagnetronTube;
import ciera.exceptions.StateMachineException;
import ciera.exceptions.XtumlException;
import ciera.statemachine.Event;
import ciera.statemachine.InstanceStateMachine;
import ciera.statemachine.StateEventMatrix;

public class MagnetronTubeInstanceStateMachine extends InstanceStateMachine {
    
    private static final int OutputPowerStableAndOff = 1;
    private static final int ReducingOutputPower = 2;
    private static final int RaisingOutputPower = 3;
    private static final int OutputPowerStableAndOn = 4;
    
    public MagnetronTubeInstanceStateMachine() {
        sem = new StateEventMatrix( new int[][]{
            { StateEventMatrix.CANNOT_HAPPEN, StateEventMatrix.CANNOT_HAPPEN, StateEventMatrix.CANNOT_HAPPEN, StateEventMatrix.CANNOT_HAPPEN },
            { RaisingOutputPower, ReducingOutputPower, OutputPowerStableAndOn, StateEventMatrix.EVENT_IGNORED },
            { RaisingOutputPower, ReducingOutputPower, OutputPowerStableAndOn, OutputPowerStableAndOff },
            { RaisingOutputPower, ReducingOutputPower, OutputPowerStableAndOn, OutputPowerStableAndOff },
            { RaisingOutputPower, ReducingOutputPower, StateEventMatrix.EVENT_IGNORED, OutputPowerStableAndOff },
        });
    }

    @Override
    protected void stateActivity(int stateNum, Event e) throws XtumlException {
        if ( stateNum == OutputPowerStableAndOff ) {
            stateOutputPowerStableAndOff( e );
        }
        else if ( stateNum == ReducingOutputPower ) {
            stateReducingOutputPower( e );
        }
        else if ( stateNum == RaisingOutputPower ) {
            stateRaisingOutputPower( e );
        }
        else if ( stateNum == OutputPowerStableAndOn ) {
            stateOutputPowerStableAndOn( e );
        }
        else throw new StateMachineException( "State does not exist. " );
    }
    
    private void stateOutputPowerStableAndOff( Event e ) {
    }

    private void stateReducingOutputPower( Event e ) throws XtumlException {
        MagnetronTube self = (MagnetronTube)getInstance();
        // if (self.current_power_output == tube_wattage::med_low)
        if ( self.getM_current_power_output() == TubeWattage.MED_LOW ) {
            // self.current_power_output = tube_wattage::low;
            self.setM_current_power_output( TubeWattage.LOW );
        }
        // elif (self.current_power_output == tube_wattage::medium)
        else if ( self.getM_current_power_output() == TubeWattage.MEDIUM ) {
            // self.current_power_output = tube_wattage::med_low;
            self.setM_current_power_output( TubeWattage.MED_LOW );
        }
        // elif (self.current_power_output == tube_wattage::med_high)
        else if ( self.getM_current_power_output() == TubeWattage.MED_HIGH ) {
            // self.current_power_output = tube_wattage::medium;
            self.setM_current_power_output( TubeWattage.MEDIUM );
        }
        // elif (self.current_power_output == tube_wattage::high)
        else if ( self.getM_current_power_output() == TubeWattage.HIGH ) {
            // self.current_power_output = tube_wattage::med_high;
            self.setM_current_power_output( TubeWattage.MED_HIGH );
        // end if;
        }
    }

    private void stateRaisingOutputPower( Event e ) throws XtumlException {
        MagnetronTube self = (MagnetronTube)getInstance();
        // if (self.current_power_output == tube_wattage::low)
        if ( self.getM_current_power_output() == TubeWattage.LOW ) {
            // self.current_power_output = tube_wattage::med_low;
            self.setM_current_power_output( TubeWattage.MED_LOW );
        }
        // elif (self.current_power_output == tube_wattage::med_low)
        else if ( self.getM_current_power_output() == TubeWattage.MED_LOW ) {
            // self.current_power_output = tube_wattage::medium;
            self.setM_current_power_output( TubeWattage.MEDIUM );
        }
        // elif (self.current_power_output == tube_wattage::medium)
        else if ( self.getM_current_power_output() == TubeWattage.MEDIUM ) {
            // self.current_power_output = tube_wattage::med_high;
            self.setM_current_power_output( TubeWattage.MED_HIGH );
        }
        // elif (self.current_power_output == tube_wattage::med_high)
        else if ( self.getM_current_power_output() == TubeWattage.MED_HIGH ) {
            // self.current_power_output = tube_wattage::high;
            self.setM_current_power_output( TubeWattage.HIGH );
        //end if;
        }
    }
    
    private void stateOutputPowerStableAndOn( Event e ) {
    }

}
