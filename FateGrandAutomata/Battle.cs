using System;

namespace FateGrandAutomata
{
    public class Battle
    {
        public bool HasClickedAttacked { get; private set; }

        public bool HasChoosenTarget { get; private set; }

        public int CurrentStage { get; private set; }
        public int CurrentTurn { get; private set; }

        public void ClickAttack() => throw new NotImplementedException();
    }
}