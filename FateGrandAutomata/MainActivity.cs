using System;
using Android.App;
using Android.Content;
using Android.Graphics;
using Android.Hardware.Display;
using Android.Media;
using Android.Media.Projection;
using Android.OS;
using Android.Runtime;
using Android.Support.Design.Widget;
using Android.Support.V7.App;
using Android.Util;
using Android.Views;
using Android.Widget;

namespace FateGrandAutomata
{
    [Activity(Label = "@string/app_name", Theme = "@style/AppTheme.NoActionBar", MainLauncher = true)]
    public class MainActivity : AppCompatActivity
    {
        protected override void OnCreate(Bundle savedInstanceState)
        {
            base.OnCreate(savedInstanceState);
            Xamarin.Essentials.Platform.Init(this, savedInstanceState);
            SetContentView(Resource.Layout.activity_main);

            var toolbar = FindViewById<Android.Support.V7.Widget.Toolbar>(Resource.Id.toolbar);
            SetSupportActionBar(toolbar);

            var fab = FindViewById<FloatingActionButton>(Resource.Id.fab);
            fab.Click += FabOnClick;

            if (savedInstanceState != null)
            {
                _resultCode = (Result) savedInstanceState.GetInt(STATE_RESULT_CODE);
                _resultData = (Intent) savedInstanceState.GetParcelable(STATE_RESULT_DATA);
            }

            var metrics = new DisplayMetrics();
            WindowManager.DefaultDisplay.GetMetrics(metrics);
            _screenDensity = (int) metrics.DensityDpi;
            _screenWidth = metrics.WidthPixels;
            _screenHeight = metrics.HeightPixels;

            _imageReader = ImageReader.NewInstance(_screenWidth, _screenHeight, (ImageFormatType)1, 1);

            _mediaProjectionManager = (MediaProjectionManager) GetSystemService(Context.MediaProjectionService);
        }

        protected override void OnSaveInstanceState(Bundle outState)
        {
            base.OnSaveInstanceState(outState);

            if (_resultData != null)
            {
                outState.PutInt(STATE_RESULT_CODE, (int)_resultCode);
                outState.PutParcelable(STATE_RESULT_DATA, _resultData);
            }
        }

        protected override void OnActivityResult(int requestCode, Result resultCode, Intent data)
        {
            if (requestCode == REQUEST_MEDIA_PROJECTION)
            {
                if (resultCode != Result.Ok)
                {
                    Toast.MakeText(this, "Canceled", ToastLength.Short).Show();
                }

                _resultCode = resultCode;
                _resultData = data;

                SetupMediaProjection();
                SetupVirtualDisplay();
            }
        }

        void SetupMediaProjection()
        {
            _mediaProjection = _mediaProjectionManager.GetMediaProjection((int) _resultCode, _resultData);
        }

        void SetupVirtualDisplay()
        {
            _virtualDisplay = _mediaProjection.CreateVirtualDisplay("ScreenCapture",
                _screenWidth, _screenHeight, _screenDensity,
                DisplayFlags.None, _imageReader.Surface, null, null);
        }

        Bitmap AcquireLatestImage()
        {
            using var img = _imageReader.AcquireLatestImage();

            var planes = img.GetPlanes();
            var buffer = planes[0].Buffer;
            var pixelStride = planes[0].PixelStride;
            var rowStride = planes[0].RowStride;
            var rowPadding = rowStride - pixelStride * _screenWidth;

            var bmp = Bitmap.CreateBitmap(_screenWidth + rowPadding / pixelStride, _screenHeight, Bitmap.Config.Argb8888);

            bmp.CopyPixelsFromBuffer(buffer);

            return bmp;
        }

        void TeardownMediaProjection()
        {
            if (_mediaProjection != null)
            {
                _mediaProjection.Stop();
                _mediaProjection = null;
            }
        }

        void StartScreenCapture()
        {
            if (_mediaProjection != null)
            {
                SetupVirtualDisplay();
            }
            else if (_resultCode != 0 && _resultData != null)
            {
                SetupMediaProjection();
                SetupVirtualDisplay();
            }
            else
            {
                // This initiates a prompt dialog for the user to confirm screen projection.
                StartActivityForResult(_mediaProjectionManager.CreateScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
            }
        }

        void StopScreenCapture()
        {
            if (_virtualDisplay == null)
            {
                return;
            }

            _virtualDisplay.Release();
            _virtualDisplay = null;

            _imageReader.Close();
            _imageReader = null;
        }

        protected override void OnPause()
        {
            base.OnPause();
            StopScreenCapture();
        }

        protected override void OnDestroy()
        {
            base.OnDestroy();
            TeardownMediaProjection();
        }

        public override bool OnCreateOptionsMenu(IMenu menu)
        {
            MenuInflater.Inflate(Resource.Menu.menu_main, menu);
            return true;
        }

        public override bool OnOptionsItemSelected(IMenuItem item)
        {
            int id = item.ItemId;
            if (id == Resource.Id.action_settings)
            {
                return true;
            }

            return base.OnOptionsItemSelected(item);
        }

        private void FabOnClick(object sender, EventArgs eventArgs)
        {
            StartScreenCapture();
        }
        public override void OnRequestPermissionsResult(int requestCode, string[] permissions, [GeneratedEnum] Android.Content.PM.Permission[] grantResults)
        {
            Xamarin.Essentials.Platform.OnRequestPermissionsResult(requestCode, permissions, grantResults);

            base.OnRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        const string STATE_RESULT_CODE = "result_code", STATE_RESULT_DATA = "result_data";

        const int REQUEST_MEDIA_PROJECTION = 1;

        int _screenDensity, _screenWidth, _screenHeight;
        Result _resultCode;
        Intent _resultData;

        ImageReader _imageReader;
        MediaProjection _mediaProjection;
        VirtualDisplay _virtualDisplay;
        MediaProjectionManager _mediaProjectionManager;
    }
}

