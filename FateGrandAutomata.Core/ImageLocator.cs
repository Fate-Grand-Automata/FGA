using System;
using CoreAutomata;

namespace FateGrandAutomata
{
    public static class ImageLocator
    {
        static Lazy<Pattern> GetLazyGeneralPattern(string Filename)
        {
            return new Lazy<Pattern>(() => new Pattern(GeneralImagePath + Filename));
        }

        const string GeneralImagePath = "images/";

        static readonly Lazy<Pattern> _battle = GetLazyGeneralPattern("battle.png");

        public static Pattern Battle => _battle.Value;

        static readonly Lazy<Pattern> _targetDanger = GetLazyGeneralPattern("target_danger.png");

        public static Pattern TargetDanger => _targetDanger.Value;

        static readonly Lazy<Pattern> _targetServant = GetLazyGeneralPattern("target_servant.png");

        public static Pattern TargetServant => _targetServant.Value;

        static readonly Lazy<Pattern> _buster = GetLazyGeneralPattern("buster.png");

        public static Pattern Buster => _buster.Value;

        static readonly Lazy<Pattern> _art = GetLazyGeneralPattern("art.png");

        public static Pattern Art => _art.Value;

        static readonly Lazy<Pattern> _quick = GetLazyGeneralPattern("quick.png");

        public static Pattern Quick => _quick.Value;

        static readonly Lazy<Pattern> _weak = GetLazyGeneralPattern("weak.png");

        public static Pattern Weak => _weak.Value;

        static readonly Lazy<Pattern> _resist = GetLazyGeneralPattern("resist.png");

        public static Pattern Resist => _resist.Value;

        static readonly Lazy<Pattern> _friend = GetLazyGeneralPattern("friend.png");

        public static Pattern Friend => _friend.Value;

        static readonly Lazy<Pattern> _limitBroken = GetLazyGeneralPattern("limitBroken.png");

        public static Pattern LimitBroken => _limitBroken.Value;

        static readonly Lazy<Pattern> _supportScreen = GetLazyGeneralPattern("support_screen.png");

        public static Pattern SupportScreen => _supportScreen.Value;

        static readonly Lazy<Pattern> _supportRegionTool = GetLazyGeneralPattern("support_region_tool.png");

        public static Pattern SupportRegionTool => _supportRegionTool.Value;

        static readonly Lazy<Pattern> _storySkip = GetLazyGeneralPattern("storyskip.png");

        public static Pattern StorySkip => _storySkip.Value;

        static readonly Lazy<Pattern> _menu = GetLazyGeneralPattern("menu.png");

        public static Pattern Menu => _menu.Value;

        static readonly Lazy<Pattern> _stamina = GetLazyGeneralPattern("stamina.png");

        public static Pattern Stamina => _stamina.Value;

        static readonly Lazy<Pattern> _result = GetLazyGeneralPattern("result.png");

        public static Pattern Result => _result.Value;

        static readonly Lazy<Pattern> _bond = GetLazyGeneralPattern("bond.png");

        public static Pattern Bond => _bond.Value;

        static readonly Lazy<Pattern> _ceReward = GetLazyGeneralPattern("ce_reward.png");

        public static Pattern Bond10Reward => _ceReward.Value;

        static readonly Lazy<Pattern> _friendRequest = GetLazyGeneralPattern("friendrequest.png");

        public static Pattern FriendRequest => _friendRequest.Value;

        static readonly Lazy<Pattern> _confirm = GetLazyGeneralPattern("confirm.png");

        public static Pattern Confirm => _confirm.Value;

        static readonly Lazy<Pattern> _questReward = GetLazyGeneralPattern("questreward.png");

        public static Pattern QuestReward => _questReward.Value;

        const string SupportImagePath = "image_SUPPORT/";

        public static Pattern LoadSupportImagePattern(string FileName)
        {
            return new Pattern(SupportImagePath + FileName);
        }
    }
}