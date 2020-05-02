using System;
using System.IO;
using Android.App;
using Android.OS;
using Android.Views;
using Android.Widget;
using AndroidX.AppCompat.App;
using AlertDialog = AndroidX.AppCompat.App.AlertDialog;
using Toolbar = AndroidX.AppCompat.Widget.Toolbar;
using Uri = Android.Net.Uri;

namespace FateGrandAutomata
{
    [Activity(Label = "Support Images", Theme = "@style/AppTheme.NoActionBar")]
    public class SupportImgNamerActivity : AppCompatActivity
    {
        public const string SupportImageIdKey = nameof(SupportImageIdKey);

        ImageView _imServant0, _imServant1, _imCe0, _imCe1;
        EditText _txtServant0, _txtServant1, _txtCe0, _txtCe1;
        ImageButton _delServant0, _delServant1, _delCe0, _delCe1;

        string _servant0Path, _servant1Path, _ce0Path, _ce1Path;

        protected override void OnCreate(Bundle SavedInstanceState)
        {
            base.OnCreate(SavedInstanceState);
            SetContentView(Resource.Layout.support_img_namer);

            var toolbar = FindViewById<Toolbar>(Resource.Id.support_img_namer_toolbar);
            SetSupportActionBar(toolbar);

            var extras = Intent.Extras;
            var supportImgId = extras.GetString(SupportImageIdKey);

            _imServant0 = FindViewById<ImageView>(Resource.Id.image_servant_0);
            _imServant1 = FindViewById<ImageView>(Resource.Id.image_servant_1);
            _imCe0 = FindViewById<ImageView>(Resource.Id.image_ce_0);
            _imCe1 = FindViewById<ImageView>(Resource.Id.image_ce_1);

            _txtServant0 = FindViewById<EditText>(Resource.Id.text_servant_0);
            _txtServant1 = FindViewById<EditText>(Resource.Id.text_servant_1);
            _txtCe0 = FindViewById<EditText>(Resource.Id.text_ce_0);
            _txtCe1 = FindViewById<EditText>(Resource.Id.text_ce_1);

            _delServant0 = FindViewById<ImageButton>(Resource.Id.del_servant_0);
            _delServant1 = FindViewById<ImageButton>(Resource.Id.del_servant_1);
            _delCe0 = FindViewById<ImageButton>(Resource.Id.del_ce_0);
            _delCe1 = FindViewById<ImageButton>(Resource.Id.del_ce_1);

            _servant0Path = SupportImageMaker.GetServantImgPath(supportImgId, 0);
            _servant1Path = SupportImageMaker.GetServantImgPath(supportImgId, 1);
            
            _ce0Path = SupportImageMaker.GetCeImgPath(supportImgId, 0);
            _ce1Path = SupportImageMaker.GetCeImgPath(supportImgId, 1);

            InitBlock(_servant0Path, _imServant0, _delServant0, HideServant0);
            InitBlock(_servant1Path, _imServant1, _delServant1, HideServant1);

            InitBlock(_ce0Path, _imCe0, _delCe0, HideCe0);
            InitBlock(_ce1Path, _imCe1, _delCe1, HideCe1);
        }

        void InitBlock(string ImgPath, ImageView ImgView, ImageButton DeleteButton, Action Hider)
        {
            if (!File.Exists(ImgPath))
            {
                Hider();
            }
            else
            {
                ImgView.SetImageURI(Uri.Parse(ImgPath));

                DeleteButton.Click += (S, E) => DeleteImg(ImgPath, Hider);
            }
        }

        void DeleteImg(string Path, Action Callback)
        {
            new AlertDialog.Builder(this)
                .SetMessage("Are you sure you want to delete this image?")
                .SetTitle("Confirm Deletion")
                .SetPositiveButton("Delete", (S, E) =>
                {
                    File.Delete(Path);
                    Callback.Invoke();
                })
                .SetNegativeButton("Cancel", (S, E) => { })
                .Show();
        }

        void HideServant0()
        {
            _imServant0.Visibility = _txtServant0.Visibility = _delServant0.Visibility = ViewStates.Gone;
            _txtServant0.Text = "";
        }

        void HideServant1()
        {
            _imServant1.Visibility = _txtServant1.Visibility = _delServant1.Visibility = ViewStates.Gone;
            _txtServant1.Text = "";
        }

        void HideCe0()
        {
            _imCe0.Visibility = _txtCe0.Visibility = _delCe0.Visibility = ViewStates.Gone;
            _txtCe0.Text = "";
        }

        void HideCe1()
        {
            _imCe1.Visibility = _txtCe1.Visibility = _delCe1.Visibility = ViewStates.Gone;
            _txtCe1.Text = "";
        }

        void ShowToast(string Msg)
        {
            Toast.MakeText(this, Msg, ToastLength.Short).Show();
        }

        bool PerformRename(string OldPath, string NewFileName, Action Hider)
        {
            if (File.Exists(OldPath) && !string.IsNullOrWhiteSpace(NewFileName))
            {
                var folder = Path.GetDirectoryName(OldPath);
                var newPath = Path.Combine(folder, $"{NewFileName}.png");

                if (File.Exists(newPath))
                {
                    ShowToast($"'{NewFileName}' already exists");
                    return false;
                }

                try
                {
                    var newPathDir = Path.GetDirectoryName(newPath);

                    if (!Directory.Exists(newPathDir))
                    {
                        Directory.CreateDirectory(newPathDir);
                    }

                    File.Move(OldPath, newPath);
                }
                catch
                {
                    ShowToast($"Failed to rename to: '{NewFileName}'");
                    return false;
                }

                Hider();
            }

            return true;
        }

        void RenameSupportImages()
        {
            if (!PerformRename(_servant0Path, _txtServant0.Text, HideServant0))
                return;

            if (!PerformRename(_servant1Path, _txtServant1.Text, HideServant1))
                return;

            if (!PerformRename(_ce0Path, _txtCe0.Text, HideCe0))
                return;

            if (!PerformRename(_ce1Path, _txtCe1.Text, HideCe1))
                return;

            Finish();
        }

        public override bool OnCreateOptionsMenu(IMenu Menu)
        {
            MenuInflater.Inflate(Resource.Menu.support_img_namer_menu, Menu);
            return true;
        }

        public override bool OnOptionsItemSelected(IMenuItem Item)
        {
            switch (Item.ItemId)
            {
                case Resource.Id.action_rename_support_imgs:
                    RenameSupportImages();
                    return true;

                case Resource.Id.action_rename_support_imgs_discard:
                    Discard();
                    return true;
            }

            return base.OnOptionsItemSelected(Item);
        }

        void Discard()
        {
            new AlertDialog.Builder(this)
                .SetMessage("Do you want to delete all images and exit?")
                .SetTitle("Confirm Deletion")
                .SetPositiveButton("Yes", (S, E) =>
                {
                    var files = new[] { _servant0Path, _servant1Path, _ce0Path, _ce1Path };

                    foreach (var file in files)
                    {
                        if (File.Exists(file))
                        {
                            File.Delete(file);
                        }
                    }
                    
                    Finish();
                })
                .SetNegativeButton("No", (S, E) => { })
                .Show();
        }

        public override void OnBackPressed()
        {
            new AlertDialog.Builder(this)
                .SetMessage("If you exit now, the images will not be renamed. Do you still want to exit?")
                .SetTitle("Confirm Exit")
                .SetPositiveButton("Yes", (S, E) =>
                {
                    base.OnBackPressed();
                })
                .SetNegativeButton("No", (S, E) => { })
                .Show();
        }
    }
}