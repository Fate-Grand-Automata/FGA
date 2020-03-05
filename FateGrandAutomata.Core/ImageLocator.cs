using System;

namespace FateGrandAutomata
{
    public static class ImageLocator
    {
        const string GeneralImagePath = "images/";

        static readonly Lazy<Pattern> _battle = new Lazy<Pattern>(() => new Pattern(GeneralImagePath + "battle.png"));

        public static Pattern Battle => _battle.Value;

        static readonly Lazy<Pattern> _targetDanger = new Lazy<Pattern>(() => new Pattern(GeneralImagePath + "target_danger.png"));

        public static Pattern TargetDanger => _targetDanger.Value;

        static readonly Lazy<Pattern> _targetServant = new Lazy<Pattern>(() => new Pattern(GeneralImagePath + "target_servant.png"));

        public static Pattern TargetServant => _targetServant.Value;

        static readonly Lazy<Pattern> _buster = new Lazy<Pattern>(() => new Pattern(GeneralImagePath + "buster.png"));

        public static Pattern Buster => _buster.Value;

        static readonly Lazy<Pattern> _art = new Lazy<Pattern>(() => new Pattern(GeneralImagePath + "art.png"));

        public static Pattern Art => _art.Value;

        static readonly Lazy<Pattern> _quick = new Lazy<Pattern>(() => new Pattern(GeneralImagePath + "quick.png"));

        public static Pattern Quick => _quick.Value;

        static readonly Lazy<Pattern> _weak = new Lazy<Pattern>(() => new Pattern(GeneralImagePath + "weak.png"));

        public static Pattern Weak => _weak.Value;

        static readonly Lazy<Pattern> _resist = new Lazy<Pattern>(() => new Pattern(GeneralImagePath + "resist.png"));

        public static Pattern Resist => _resist.Value;

        static readonly Lazy<Pattern> _friend = new Lazy<Pattern>(() => new Pattern(GeneralImagePath + "friend.png"));

        public static Pattern Friend => _friend.Value;

        static readonly Lazy<Pattern> _limitBroken = new Lazy<Pattern>(() => new Pattern(GeneralImagePath + "limitBroken.png"));

        public static Pattern LimitBroken => _limitBroken.Value;

        static readonly Lazy<Pattern> _supportScreen = new Lazy<Pattern>(() => new Pattern(GeneralImagePath + "support_screen.png"));

        public static Pattern SupportScreen => _supportScreen.Value;

        static readonly Lazy<Pattern> _supportRegionTool = new Lazy<Pattern>(() => new Pattern(GeneralImagePath + "support_region_tool.png"));

        public static Pattern SupportRegionTool => _supportRegionTool.Value;

        const string SupportImagePath = "image_SUPPORT/";

        public static Pattern LoadSupportImagePattern(string FileName)
        {
            return new Pattern(SupportImagePath + FileName);
        }
    }
}