﻿using System.Collections.Generic;
using System.IO;
using System.Reflection;
using CoreAutomata;

namespace FateGrandAutomata
{
    public static class ImageLocator
    {
        static Stream FileLoader(string Filename)
        {
            var filepath = Path.Combine(SupportImgFolder, Filename);

            return File.Exists(filepath)
                ? File.OpenRead(filepath)
                : null;
        }

        public static string SupportImgFolder
        {
            get
            {
                var dir = Path.Combine(AutomataApi.StorageDir, "support");

                if (!Directory.Exists(dir))
                {
                    Directory.CreateDirectory(dir);
                }

                return dir;
            }
        }

        public static string SupportServantImgFolder
        {
            get
            {
                var dir = Path.Combine(SupportImgFolder, "servant");

                if (!Directory.Exists(dir))
                {
                    Directory.CreateDirectory(dir);
                }

                return dir;
            }
        }

        public static string SupportCeImgFolder
        {
            get
            {
                var dir = Path.Combine(SupportImgFolder, "ce");

                if (!Directory.Exists(dir))
                {
                    Directory.CreateDirectory(dir);
                }

                return dir;
            }
        }

        static IPattern CreatePattern(string FilePath)
        {
            var assembly = Assembly.GetExecutingAssembly();
            var resourceName = $"{nameof(FateGrandAutomata)}.{FilePath}";

            var stream = assembly.GetManifestResourceStream(resourceName);

            if (stream == null)
            {
                return null;
            }

            using (stream)
            {
                return AutomataApi.LoadPattern(stream);
            }
        }

        static GameServer _currentGameServer;
        static readonly Dictionary<string, IPattern> RegionCachedPatterns = new Dictionary<string, IPattern>();

        static IPattern GetRegionPattern(string Filename)
        {
            var server = Preferences.Instance.GameServer;

            // Reload Patterns on Server change
            if (_currentGameServer != server)
            {
                foreach (var pattern in RegionCachedPatterns.Values)
                {
                    pattern.Dispose();
                }

                RegionCachedPatterns.Clear();

                _currentGameServer = server;
            }

            if (!RegionCachedPatterns.ContainsKey(Filename))
            {
                var pattern = CreatePattern($"images.{Preferences.Instance.GameServer}.{Filename}");
                
                RegionCachedPatterns.Add(Filename, pattern);
            }

            return RegionCachedPatterns[Filename];
        }

        public static IPattern Battle => GetRegionPattern("battle.png");

        public static IPattern TargetDanger => GetRegionPattern("target_danger.png");

        public static IPattern TargetServant => GetRegionPattern("target_servant.png");

        public static IPattern Buster => GetRegionPattern("buster.png");

        public static IPattern Art => GetRegionPattern("art.png");

        public static IPattern Quick => GetRegionPattern("quick.png");

        public static IPattern Weak => GetRegionPattern("weak.png");

        public static IPattern Resist => GetRegionPattern("resist.png");

        public static IPattern Friend => GetRegionPattern("friend.png");

        public static IPattern LimitBroken => GetRegionPattern("limitBroken.png");

        public static IPattern SupportScreen => GetRegionPattern("support_screen.png");

        public static IPattern SupportRegionTool => GetRegionPattern("support_region_tool.png");

        public static IPattern StorySkip => GetRegionPattern("storyskip.png");

        public static IPattern Menu => GetRegionPattern("menu.png");

        public static IPattern Stamina => GetRegionPattern("stamina.png");

        public static IPattern Result => GetRegionPattern("result.png");

        public static IPattern Bond => GetRegionPattern("bond.png");

        public static IPattern Bond10Reward => GetRegionPattern("ce_reward.png");

        public static IPattern FriendRequest => GetRegionPattern("friendrequest.png");

        public static IPattern Confirm => GetRegionPattern("confirm.png");

        public static IPattern QuestReward => GetRegionPattern("questreward.png");

        public static IPattern Retry => GetRegionPattern("retry.png");

        public static IPattern Withdraw => GetRegionPattern("withdraw.png");

        public static IPattern FinishedLotteryBox => GetRegionPattern("lottery.png");

        public static IPattern PresentBoxFull => GetRegionPattern("StopGifts.png");

        public static IPattern LoadSupportImagePattern(string FileName)
        {
            var pattern = CreatePattern($"images.Support.{FileName}");

            if (pattern != null)
            {
                return pattern;
            }

            var stream = FileLoader(FileName);

            if (stream == null)
            {
                throw new ScriptExitException($"Unable to load image: {FileName}. Put images in {SupportImgFolder} folder");
            }

            using (stream)
            {
                return AutomataApi.LoadPattern(stream);
            }
        }
    }
}