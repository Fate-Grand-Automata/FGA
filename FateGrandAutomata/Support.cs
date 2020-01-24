using System.Collections.Generic;

namespace FateGrandAutomata
{
    public class Support
    {
        const string SupportImagePath = "image_SUPPORT/";

        const int CraftEssenceHeight = 90;

        const char LimitBrokenCharacter = '*';

        readonly List<string> _preferredServantArray = new List<string>(),
            _friendNameArray = new List<string>();

        readonly List<(string Name, bool PreferMlb)> _preferredCraftEssenceTable = new List<(string Name, bool PreferMlb)>();

        public void Init()
        {
            static IEnumerable<string> Split(string Str)
            {
                foreach (var s in Str.Split(','))
                {
                    var val = s.Trim();

                    if (val.ToLower() != "any")
                    {
                        yield return val;
                    }
                }
            }

            // Friend names
            foreach (var friend in Split(Preferences.SupportFriendNames))
            {
                _friendNameArray.Add(friend.Trim());
            }

            // Servants
            foreach (var servant in Split(Preferences.SupportPreferredServants))
            {
                _preferredServantArray.Add(servant.Trim());
            }

            // Craft essences
            foreach (var craftEssence in Split(Preferences.SupportPreferredCEs))
            {
                _preferredCraftEssenceTable.Add((
                    craftEssence.Replace(LimitBrokenCharacter.ToString(), ""),
                    craftEssence.StartsWith(LimitBrokenCharacter)));
            }
        }
    }
}