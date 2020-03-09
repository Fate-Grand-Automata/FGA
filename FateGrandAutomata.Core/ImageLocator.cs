using System;
using System.Reflection;
using CoreAutomata;

namespace FateGrandAutomata
{
    public static class ImageLocator
    {
        static IPattern CreatePattern(string FilePath)
        {
            var assembly = Assembly.GetExecutingAssembly();
            var resourceName = $"{nameof(FateGrandAutomata)}.{FilePath}";

            var stream = assembly.GetManifestResourceStream(resourceName);

            return AutomataApi.LoadPattern(stream);
        }

        static Lazy<IPattern> GetLazyGeneralPattern(string Filename)
        {
            return new Lazy<IPattern>(() => CreatePattern($"images.{Preferences.GameServer}.{Filename}"));
        }

        static readonly Lazy<IPattern> _battle = GetLazyGeneralPattern("battle.png");

        public static IPattern Battle => _battle.Value;

        static readonly Lazy<IPattern> _targetDanger = GetLazyGeneralPattern("target_danger.png");

        public static IPattern TargetDanger => _targetDanger.Value;

        static readonly Lazy<IPattern> _targetServant = GetLazyGeneralPattern("target_servant.png");

        public static IPattern TargetServant => _targetServant.Value;

        static readonly Lazy<IPattern> _buster = GetLazyGeneralPattern("buster.png");

        public static IPattern Buster => _buster.Value;

        static readonly Lazy<IPattern> _art = GetLazyGeneralPattern("art.png");

        public static IPattern Art => _art.Value;

        static readonly Lazy<IPattern> _quick = GetLazyGeneralPattern("quick.png");

        public static IPattern Quick => _quick.Value;

        static readonly Lazy<IPattern> _weak = GetLazyGeneralPattern("weak.png");

        public static IPattern Weak => _weak.Value;

        static readonly Lazy<IPattern> _resist = GetLazyGeneralPattern("resist.png");

        public static IPattern Resist => _resist.Value;

        static readonly Lazy<IPattern> _friend = GetLazyGeneralPattern("friend.png");

        public static IPattern Friend => _friend.Value;

        static readonly Lazy<IPattern> _limitBroken = GetLazyGeneralPattern("limitBroken.png");

        public static IPattern LimitBroken => _limitBroken.Value;

        static readonly Lazy<IPattern> _supportScreen = GetLazyGeneralPattern("support_screen.png");

        public static IPattern SupportScreen => _supportScreen.Value;

        static readonly Lazy<IPattern> _supportRegionTool
            = new Lazy<IPattern>(() => LoadSupportImagePattern("support_region_tool.png"));

        public static IPattern SupportRegionTool => _supportRegionTool.Value;

        static readonly Lazy<IPattern> _storySkip = GetLazyGeneralPattern("storyskip.png");

        public static IPattern StorySkip => _storySkip.Value;

        static readonly Lazy<IPattern> _menu = GetLazyGeneralPattern("menu.png");

        public static IPattern Menu => _menu.Value;

        static readonly Lazy<IPattern> _stamina = GetLazyGeneralPattern("stamina.png");

        public static IPattern Stamina => _stamina.Value;

        static readonly Lazy<IPattern> _result = GetLazyGeneralPattern("result.png");

        public static IPattern Result => _result.Value;

        static readonly Lazy<IPattern> _bond = GetLazyGeneralPattern("bond.png");

        public static IPattern Bond => _bond.Value;

        static readonly Lazy<IPattern> _ceReward = GetLazyGeneralPattern("ce_reward.png");

        public static IPattern Bond10Reward => _ceReward.Value;

        static readonly Lazy<IPattern> _friendRequest = GetLazyGeneralPattern("friendrequest.png");

        public static IPattern FriendRequest => _friendRequest.Value;

        static readonly Lazy<IPattern> _confirm = GetLazyGeneralPattern("confirm.png");

        public static IPattern Confirm => _confirm.Value;

        static readonly Lazy<IPattern> _questReward = GetLazyGeneralPattern("questreward.png");

        public static IPattern QuestReward => _questReward.Value;

        public static IPattern LoadSupportImagePattern(string FileName)
        {
            return CreatePattern($"images.Support.{FileName}");
        }
    }
}