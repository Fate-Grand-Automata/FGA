﻿using System;
using System.Collections.Generic;
using System.Linq;
using Android.App;
using Android.Content;
using Android.OS;
using Android.Widget;
using AndroidX.AppCompat.App;
using AndroidX.Preference;
using Google.Android.Material.FloatingActionButton;

namespace FateGrandAutomata
{
    [Activity(Label = "AutoSkill List")]
    public class AutoSkillActivity : AppCompatActivity
    {
        protected override void OnCreate(Bundle SavedInstanceState)
        {
            base.OnCreate(SavedInstanceState);
            SetContentView(Resource.Layout.autoskill_list);

            var addBtn = FindViewById<FloatingActionButton>(Resource.Id.autoskill_add_btn);
            addBtn.Click += AddBtnOnClick;

            var listView = FindViewById<ListView>(Resource.Id.autoskill_listview);
            listView.ItemClick += ListViewOnItemClick;

            InitView();
        }

        // Handle back button
        protected override void OnRestart()
        {
            base.OnRestart();

            InitView();
        }

        (string Id, string Name)[] _autoSkillItems;

        void InitView()
        {
            var listView = FindViewById<ListView>(Resource.Id.autoskill_listview);
            var prefManager = PreferenceManager.GetDefaultSharedPreferences(this);
            _autoSkillItems = prefManager.GetStringSet(GetString(Resource.String.pref_autoskill_list), new List<string>())
                .Select(M =>
                {
                    var sharedPrefs = GetSharedPreferences(M, FileCreationMode.Private);

                    return (Id: M, Name: sharedPrefs.GetString(GetString(Resource.String.pref_autoskill_name), "--"));
                })
                .OrderBy(M => M.Name)
                .ToArray();

            var autoSkillNames = _autoSkillItems
                .Select(M => M.Name)
                .ToArray();

            var adapter = new ArrayAdapter<string>(this, Resource.Layout.autoskill_item, autoSkillNames);
            listView.Adapter = adapter;
        }

        void ListViewOnItemClick(object Sender, AdapterView.ItemClickEventArgs E)
        {
            var guid = _autoSkillItems[E.Position].Id;

            EditItem(guid);
        }

        void EditItem(string ItemKey)
        {
            var intent = new Intent(this, typeof(AutoSkillItemActivity));
            intent.PutExtra(AutoSkillItemActivity.AutoSkillItemKey, ItemKey);

            StartActivity(intent);
        }

        void AddBtnOnClick(object Sender, EventArgs E)
        {
            var guid = Guid.NewGuid().ToString();

            var key = GetString(Resource.String.pref_autoskill_list);

            var prefs = PreferenceManager.GetDefaultSharedPreferences(this);
            var autoSkillItems = prefs.GetStringSet(key, new List<string>())
                .ToList(); // Make a copy

            autoSkillItems.Add(guid);
            prefs
                .Edit()
                .PutStringSet(key, autoSkillItems)
                .Commit();

            EditItem(guid);
        }
    }
}