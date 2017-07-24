package MicrowaveOvenCiera.components.MicrowaveOven.MicrowaveOven.InstanceStateMachine;

import ciera.statemachine.Event;
import ciera.statemachine.EventTarget;

public class BeepDelayOver extends Event {
    
    private static final int eventNumber = 2;
    private static final int classNumber = 4;

    public BeepDelayOver(EventTarget t, boolean ts) {
        super(t, ts);
    }

    @Override
    public int getEventNumber() {
        return eventNumber;
    }

    @Override
    public int getClassNumber() {
        return classNumber;
    }

}
