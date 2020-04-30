using System;
using System.IO;
using Android.App;
using Android.OS;
using Android.Views;
using Android.Widget;
using AndroidX.AppCompat.App;
using Google.Android.Material.FloatingActionButton;
using Uri = Android.Net.Uri;

namespace FateGrandAutomata
{
    [Activity(Label = "Support Image Namer")]
    public class SupportImgNamerActivity : AppCompatActivity
    {
        public const string SupportImageIdKey = nameof(SupportImageIdKey);

        ImageView _imServant0, _imServant1, _imCe0, _imCe1;
        EditText _txtServant0, _txtServant1, _txtCe0, _txtCe1;

        string _servant0Path, _servant1Path, _ce0Path, _ce1Path;

        protected override void OnCreate(Bundle SavedInstanceState)
        {
            base.OnCreate(SavedInstanceState);
            SetContentView(Resource.Layout.support_img_namer);

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

            _servant0Path = SupportImageMaker.GetServantImgPath(supportImgId, 0);
            _servant1Path = SupportImageMaker.GetServantImgPath(supportImgId, 1);
            
            _ce0Path = SupportImageMaker.GetCeImgPath(supportImgId, 0);
            _ce1Path = SupportImageMaker.GetCeImgPath(supportImgId, 1);

            _imServant0.SetImageURI(Uri.Parse(_servant0Path));
            _txtServant0.Text = Path.GetFileNameWithoutExtension(_servant0Path);

            if (!File.Exists(_servant1Path))
            {
                _imServant1.Visibility = _txtServant1.Visibility = ViewStates.Gone;
            }
            else
            {
                _imServant1.SetImageURI(Uri.Parse(_servant1Path));
                _txtServant1.Text = Path.GetFileNameWithoutExtension(_servant1Path);
            }

            _imCe0.SetImageURI(Uri.Parse(_ce0Path));
            _txtCe0.Text = Path.GetFileNameWithoutExtension(_ce0Path);

            if (!File.Exists(_ce1Path))
            {
                _imCe1.Visibility = _txtCe1.Visibility = ViewStates.Gone;
            }
            else
            {
                _imCe1.SetImageURI(Uri.Parse(_ce1Path));
                _txtCe1.Text = Path.GetFileNameWithoutExtension(_ce1Path);
            }

            var btn = FindViewById<FloatingActionButton>(Resource.Id.btn_support_img_rename);
            btn.Click += RenameSupportImages;
        }

        void RenameSupportImages(object Sender, EventArgs E)
        {
            var newServant0Path = Path.Combine(ImageLocator.SupportServantImgFolder, $"{_txtServant0.Text}.png");
            File.Move(_servant0Path, newServant0Path);

            if (File.Exists(_servant1Path))
            {
                var newServant1Path = Path.Combine(ImageLocator.SupportServantImgFolder, $"{_txtServant1.Text}.png");
                File.Move(_servant1Path, newServant1Path);
            }

            var newCe0Path = Path.Combine(ImageLocator.SupportCeImgFolder, $"{_txtCe0.Text}.png");
            File.Move(_ce0Path, newCe0Path);

            if (File.Exists(_ce1Path))
            {
                var newCe1Path = Path.Combine(ImageLocator.SupportCeImgFolder, $"{_txtCe1.Text}.png");
                File.Move(_ce1Path, newCe1Path);
            }

            Finish();
        }
    }
}