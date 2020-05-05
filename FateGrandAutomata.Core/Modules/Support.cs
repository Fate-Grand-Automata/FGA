using System;
using System.Collections.Generic;
using System.Linq;
using CoreAutomata;

namespace FateGrandAutomata
{
    public class Support
    {
        const int CraftEssenceHeight = 90;

        public const string LimitBrokenCharacter = "*";

        readonly List<string> _preferredServantArray = new List<string>(),
            _friendNameArray = new List<string>();

        readonly List<(string Name, bool PreferMlb)> _preferredCraftEssenceTable = new List<(string Name, bool PreferMlb)>();

        public void Init()
        {
            static IEnumerable<string> Split(string Str)
            {
                if (Str == null)
                    yield break;

                foreach (var s in Str.Split(",".ToCharArray(), StringSplitOptions.RemoveEmptyEntries))
                {
                    var val = s.Trim();

                    if (val.ToLower() != "any")
                    {
                        yield return val;
                    }
                }
            }

            // Friend names
            foreach (var friend in Split(Preferences.Instance.Support.FriendNames))
            {
                _friendNameArray.Add(friend);
            }

            // Servants
            foreach (var servant in Split(Preferences.Instance.Support.PreferredServants))
            {
                _preferredServantArray.Add(servant);
            }

            // Craft essences
            foreach (var craftEssence in Split(Preferences.Instance.Support.PreferredCEs))
            {
                _preferredCraftEssenceTable.Add((
                    craftEssence.Replace(LimitBrokenCharacter, ""),
                    craftEssence.StartsWith(LimitBrokenCharacter)));
            }
        }

        public bool SelectSupport(SupportSelectionMode SelectionMode)
        {
            var pattern = ImageLocator.SupportScreen;
            while (!Game.SupportScreenRegion.Exists(pattern)) { }

            switch (SelectionMode)
            {
                case SupportSelectionMode.First:
                    return SelectFirst();

                case SupportSelectionMode.Manual:
                    SelectManual();
                    break;

                case SupportSelectionMode.Friend:
                    return SelectFriend();

                case SupportSelectionMode.Preferred:
                    var searchmethod = DecideSearchMethod();
                    return SelectPreferred(searchmethod);

                default:
                    throw new ScriptExitException("Invalid support selection mode");
            }

            return false;
        }

        void SelectManual()
        {
            throw new ScriptExitException("Support selection set to Manual");
        }

        bool SelectFirst()
        {
            TimeSpan.FromSeconds(1).Wait();
            Game.SupportFirstSupportClick.Click();

            var pattern = ImageLocator.SupportScreen;

            // https://github.com/29988122/Fate-Grand-Order_Lua/issues/192 , band-aid fix but it's working well.
            if (Game.SupportScreenRegion.Exists(pattern))
            {
                TimeSpan.FromSeconds(2).Wait();

                while (Game.SupportScreenRegion.Exists(pattern))
                {
                    TimeSpan.FromSeconds(10).Wait();
                    Game.SupportUpdateClick.Click();
                    TimeSpan.FromSeconds(1).Wait();
                    Game.SupportUpdateYesClick.Click();
                    TimeSpan.FromSeconds(3).Wait();
                    Game.SupportFirstSupportClick.Click();
                    TimeSpan.FromSeconds(1).Wait();
                }
            }

            return true;
        }

        (SupportSearchResult Result, Region Support) SearchVisible(SearchFunction SearchMethod)
        {
            (SupportSearchResult Result, Region Support) PerformSearch()
            {
                if (!IsFriend(Game.SupportFriendRegion))
                {
                    // no friends on screen, so there's no point in scrolling anymore
                    return (SupportSearchResult.NoFriendsFound, null);
                }

                var (support, bounds) = SearchMethod();

                if (support == null)
                {
                    // nope, not found this time. keep scrolling
                    return (SupportSearchResult.NotFound, null);
                }

                // bounds are already returned by searchMethod.byServantAndCraftEssence, but not by the other methods
                bounds ??= FindSupportBounds(support);

                if (!IsFriend(bounds))
                {
                    // found something, but it doesn't belong to a friend. keep scrolling
                    return (SupportSearchResult.NotFound, null);
                }

                return (SupportSearchResult.Found, support);
            }

            return AutomataApi.UseSameSnapIn(PerformSearch);
        }

        bool SelectFriend()
        {
            if (_friendNameArray.Count > 0)
            {
                return SelectPreferred(() => (FindFriendName(), null));
            }

            throw new ScriptExitException("When using 'friend' support selection mode, specify at least one friend name.");
        }

        bool SelectPreferred(SearchFunction SearchMethod)
        {
            var numberOfSwipes = 0;
            var numberOfUpdates = 0;

            while (true)
            {
                var (result, support) = SearchVisible(SearchMethod);

                if (result == SupportSearchResult.Found)
                {
                    support.Click();
                    return true;
                }

                if (result == SupportSearchResult.NotFound && numberOfSwipes < Preferences.Instance.Support.SwipesPerUpdate)
                {
                    ScrollList();
                    ++numberOfSwipes;
                    TimeSpan.FromSeconds(0.3).Wait();
                }

                else if (numberOfUpdates < Preferences.Instance.Support.MaxUpdates)
                {
                    AutomataApi.Toast("Support list will be updated in 3 seconds.");
                    TimeSpan.FromSeconds(3).Wait();

                    Game.SupportUpdateClick.Click();
                    TimeSpan.FromSeconds(1).Wait();
                    Game.SupportUpdateYesClick.Click();

                    while (Game.NeedsToRetry())
                    {
                        Game.Retry();
                    }

                    TimeSpan.FromSeconds(3).Wait();

                    ++numberOfUpdates;
                    numberOfSwipes = 0;
                }

                else
                {
                    // -- okay, we have run out of options, let's give up
                    Game.SupportListTopClick.Click();
                    return SelectSupport(Preferences.Instance.Support.FallbackTo);
                }
            }
        }

        SearchFunction DecideSearchMethod()
        {
            var hasServants = _preferredServantArray.Count > 0;
            var hasCraftEssences = _preferredCraftEssenceTable.Count > 0;

            if (hasServants && hasCraftEssences)
            {
                return () =>
                {
                    var servants = FindServants();

                    foreach (var servant in servants)
                    {
                        var supportBounds = FindSupportBounds(servant);
                        var craftEssence = FindCraftEssence(supportBounds);

                        // CEs are always below Servants in the support list
                        // see docs/support_list_edge_case_fix.png to understand why this conditional exists
                        if (craftEssence != null && craftEssence.Y > servant.Y)
                        {
                            // only return if found. if not, try the other servants before scrolling
                            return (craftEssence, supportBounds);
                        }
                    }

                    // not found, continue scrolling
                    return (null, null);
                };
            }

            if (hasServants)
            {
                return () => (FindServants().FirstOrDefault(), null);
            }

            if (hasCraftEssences)
            {
                return () => (FindCraftEssence(Game.SupportListRegion), null);
            }

            throw new ScriptExitException("When using 'preferred' support selection mode, specify at least one Servant or Craft Essence.");
        }

        void ScrollList()
        {
            AutomataApi.Swipe(Game.SupportSwipeStartClick, Game.SupportSwipeEndClick);
        }

        Region FindFriendName()
        {
            foreach (var friendName in _friendNameArray)
            {
                // Cached pattern. Don't dipose here.
                var pattern = ImageLocator.LoadSupportImagePattern(friendName);

                foreach (var theFriend in Game.SupportFriendsRegion.FindAll(pattern))
                {
                    return theFriend;
                }
            }

            return null;
        }

        IEnumerable<Region> FindServants()
        {
            foreach (var preferredServant in _preferredServantArray)
            {
                // Cached pattern. Don't dipose here.
                var pattern = ImageLocator.LoadSupportImagePattern(preferredServant);

                foreach (var servant in Game.SupportListRegion.FindAll(pattern))
                {
                    yield return servant;
                }
            }
        }

        Region FindCraftEssence(Region SearchRegion)
        {
            foreach (var preferredCraftEssence in _preferredCraftEssenceTable)
            {
                // Cached pattern. Don't dipose here.
                var pattern = ImageLocator.LoadSupportImagePattern(preferredCraftEssence.Name);

                var craftEssences = SearchRegion.FindAll(pattern);

                foreach (var craftEssence in craftEssences)
                {
                    if (!preferredCraftEssence.PreferMlb || IsLimitBroken(craftEssence))
                    {
                        return craftEssence;
                    }
                }
            }

            return null;
        }

        public const double SupportRegionToolSimilarity = 0.75;

        Region FindSupportBounds(Region Support)
        {
            var supportBound = new Region(76, 0, 2356, 428);
            var regionAnchor = ImageLocator.SupportRegionTool;

            var searchRegion = new Region(2100, 0, 300, 1440);
            var regionArray = searchRegion.FindAll(regionAnchor, SupportRegionToolSimilarity);

            var defaultRegion = supportBound;

            foreach (var testRegion in regionArray)
            {
                supportBound.Y = testRegion.Y - 70;

                if (supportBound.Contains(Support))
                {
                    return supportBound;
                }
            }

            // AutomataApi.Toast("Default Region being returned; file an issue on the github for this issue");
            return defaultRegion;
        }

        bool IsFriend(Region Region)
        {
            var friendPattern = ImageLocator.Friend;

            return !Preferences.Instance.Support.FriendsOnly || Region.Exists(friendPattern);
        }

        bool IsLimitBroken(Region CraftEssence)
        {
            var limitBreakRegion = Game.SupportLimitBreakRegion;
            limitBreakRegion.Y = CraftEssence.Y;

            var limitBreakPattern = ImageLocator.LimitBroken;

            return limitBreakRegion.Exists(limitBreakPattern, Similarity: 0.8);
        }
    }
}