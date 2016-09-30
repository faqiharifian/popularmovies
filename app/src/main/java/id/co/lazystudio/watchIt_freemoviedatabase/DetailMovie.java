package id.co.lazystudio.watchIt_freemoviedatabase;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import co.lujun.androidtagview.TagContainerLayout;
import co.lujun.androidtagview.TagView;
import id.co.lazystudio.watchIt_freemoviedatabase.connection.TmdbClient;
import id.co.lazystudio.watchIt_freemoviedatabase.connection.TmdbService;
import id.co.lazystudio.watchIt_freemoviedatabase.entity.Collection;
import id.co.lazystudio.watchIt_freemoviedatabase.entity.Company;
import id.co.lazystudio.watchIt_freemoviedatabase.entity.Genre;
import id.co.lazystudio.watchIt_freemoviedatabase.entity.Image;
import id.co.lazystudio.watchIt_freemoviedatabase.entity.Keyword;
import id.co.lazystudio.watchIt_freemoviedatabase.entity.Movie;
import id.co.lazystudio.watchIt_freemoviedatabase.entity.Video;
import id.co.lazystudio.watchIt_freemoviedatabase.parser.MovieParser;
import id.co.lazystudio.watchIt_freemoviedatabase.utils.Utils;
import retrofit2.Call;
import retrofit2.Response;

public class DetailMovie extends AppCompatActivity {
    public static final String MOVIE_KEY = "movie";
    private Movie mMovie;
    private Collection mCollection = null;
    private List<Company> mCompanyList = new ArrayList<>();
    private List<Genre> mGenreList = new ArrayList<>();
    private List<Image> mBackdropList = new ArrayList<>();
    private List<Image> mPosterList = new ArrayList<>();
    private List<Keyword> mKeywordList = new ArrayList<>();
    private List<Video> mVideo = new ArrayList<>();

    ImageView backdropImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_movie);

        Bundle args = getIntent().getExtras();
        mMovie = args.getParcelable(MOVIE_KEY);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(mMovie.getTitle());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            toolbar.setTitleTextColor(getColorWithAlpha(0, getResources().getColor(android.R.color.white, getTheme())));
        else
            toolbar.setTitleTextColor(getColorWithAlpha(0, getResources().getColor(android.R.color.white)));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark, null));
            else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        backdropImageView = (ImageView) findViewById(R.id.backdrop_imageview);

        final ScrollView detailScrollView = ((ScrollView) findViewById(R.id.movie_detail_scrollview));
        detailScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                float alpha = (float) detailScrollView.getScrollY() / backdropImageView.getBottom();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    toolbar.setTitleTextColor(getColorWithAlpha(alpha, getResources().getColor(android.R.color.white, getTheme())));
                    toolbar.setBackgroundColor(getColorWithAlpha(alpha, getResources().getColor(R.color.colorPrimary, getTheme())));
                }else {
                    toolbar.setTitleTextColor(getColorWithAlpha(alpha, getResources().getColor(android.R.color.white)));
                    toolbar.setBackgroundColor(getColorWithAlpha(alpha, getResources().getColor(R.color.colorPrimary)));
                }
//                toolbar.setTitleTextColor(getColorWithAlpha(alpha, getResources().getColor(R.color.dark_grey)));
            }
        });

        getMovie();
        populateView();
    }

    private void getMovie(){
        final ProgressBar pb = (ProgressBar) findViewById(R.id.detail_movie_progressbar);
        if(Utils.isInternetConnected(this)) {
            TmdbService tmdbService =
                    TmdbClient.getClient().create(TmdbService.class);

            final Call<MovieParser> movie = tmdbService.getMovie(mMovie.getId());

            movie.enqueue(new retrofit2.Callback<MovieParser>() {
                @Override
                public void onResponse(Call<MovieParser> call, Response<MovieParser> response) {
                    MovieParser movieParser = response.body();
                    mMovie = (Movie)response.body();
                    mCollection = movieParser.getCollection();
                    mCompanyList = movieParser.getCompanies();
                    mGenreList = movieParser.getGenres();
                    mBackdropList = movieParser.getBackdrops();
                    mPosterList = movieParser.getPosters();
                    mKeywordList = movieParser.getKeywords();
                    mVideo = movieParser.getVideos();
                    pb.setVisibility(View.GONE);
                    populateView();
                }

                @Override
                public void onFailure(Call<MovieParser> call, Throwable t) {
                    t.printStackTrace();
                    pb.setVisibility(View.GONE);
                }
            });
        }else {
            pb.setVisibility(View.GONE);
        }
    }

    private void populateView(){
        backdropImageView.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) backdropImageView.getLayoutParams();
                params.height = backdropImageView.getWidth() * 9 / 16;
            }
        });

        Picasso.with(this)
                .load(mMovie.getBackdropPath(this, 1))
                .error(R.drawable.no_image_land)
                .into(backdropImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        findViewById(R.id.backdrop_progressbar).setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {

                    }
                });

        final ImageView posterImageView = (ImageView) findViewById(R.id.poster_imageview);
        posterImageView.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) posterImageView.getLayoutParams();
                params.height = posterImageView.getWidth() * 3 / 2;

                LinearLayout container = (LinearLayout) findViewById(R.id.content_container);
                RelativeLayout.LayoutParams containerParams = (RelativeLayout.LayoutParams) container.getLayoutParams();
                containerParams.setMargins(0, -(params.height / 3), 0, 0);

                FrameLayout movieFrameLayout = (FrameLayout) findViewById(R.id.movie_title_framelayout);
                LinearLayout.LayoutParams titleParams = (LinearLayout.LayoutParams) movieFrameLayout.getLayoutParams();
                titleParams.setMargins(0, params.height / 3, 0, 0);
            }
        });

        Picasso.with(this)
                .load(mMovie.getPosterPath(this, 0))
                .error(R.drawable.no_image_port)
                .into(posterImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        findViewById(R.id.poster_progressbar).setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {

                    }
                });

        TextView titleTextView = ((TextView) findViewById(R.id.movie_title_textview));
        titleTextView.setText(mMovie.getTitle());
        ((TextView) findViewById(R.id.movie_releasedate_textview)).setText(mMovie.getReleaseDate());
        ((TextView) findViewById(R.id.movie_runtime_textview)).setText(mMovie.getRuntime());
        TextView rateAvgTextView = ((TextView) findViewById(R.id.movie_rateaverage_textview));
        rateAvgTextView.setText(mMovie.getVoteAverage());
        TextView rateCountTextView = ((TextView) findViewById(R.id.movie_ratecount_textview));
        if(mMovie.getVoteCount() > 0){
            rateCountTextView.setVisibility(View.VISIBLE);
            rateCountTextView.setText(String.valueOf(mMovie.getVoteCount()));
        }
        TextView budgetTextView = (TextView) findViewById(R.id.movie_budget_textview);
        budgetTextView.setText(mMovie.getBudget());
        TextView revenueTextView = (TextView) findViewById(R.id.movie_revenue_textview);
        revenueTextView.setText(mMovie.getRevenue());

        ((TextView) findViewById(R.id.movie_popularity_textview)).setText(mMovie.getPopularity());

        if(mMovie.getTagline() != null) {
            if(!mMovie.getTagline().equals("")) {
                final RelativeLayout taglineRelativeLayout = (RelativeLayout) findViewById(R.id.movie_tagline_relativelayout);
                taglineRelativeLayout.setVisibility(View.VISIBLE);
                taglineRelativeLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        final TextView taglineTextView = ((TextView) findViewById(R.id.movie_tagline_textview));
                        taglineTextView.setText(mMovie.getTagline());
                        taglineTextView.setMaxWidth(taglineRelativeLayout.getWidth() - (2 * getResources().getDimensionPixelSize(R.dimen.tag_width)));
                    }
                });

            }
        }

        if(mMovie.getOverview() != null){
            if(!mMovie.getOverview().equals("")){
                findViewById(R.id.movie_overview_relativelayout).setVisibility(View.VISIBLE);
                TextView overviewTextView = (TextView) findViewById(R.id.movie_overview_textview);
                overviewTextView.setText(mMovie.getOverview());
            }
        }

        if(mGenreList.size() > 0){
            TagContainerLayout genreContainer = (TagContainerLayout) findViewById(R.id.genre_tagcontainer);
            genreContainer.setVisibility(View.VISIBLE);
            String[] tags = new String[mGenreList.size()];
            for(int i = 0; i < mGenreList.size(); i++){
                tags[i] = mGenreList.get(i).getName();
            }
            genreContainer.setTags(tags);
            genreContainer.setOnTagClickListener(new TagView.OnTagClickListener() {
                @Override
                public void onTagClick(int position, String text) {
                    Genre genre = mGenreList.get(position);
                    Log.e("genre clicked", genre.getId()+" - "+genre.getName());
                }

                @Override
                public void onTagLongClick(int position, String text) {

                }
            });
        }

        if(mCompanyList.size() > 0){
//            RelativeLayout companyRelativeLayout = (RelativeLayout) findViewById(R.id.movie_productioncompany_container_relativelayout);
//            companyRelativeLayout.setVisibility(View.VISIBLE);
//
//            TextView companyTextView = (TextView) findViewById(R.id.movie_productioncompany_textview);
//            companyTextView.setText(mCompanyList.get(0).getName());
//            ImageView companyImageView = (ImageView) findViewById(R.id.movie_productioncompany_imageview);
//
//            Picasso.with(this)
//                    .load(mCompanyList.get(0).getLogoPath(this, 0))
//                    .error(R.drawable.no_image_land)
//                    .into(companyImageView);
//            Log.e("company", mCompanyList.get(0).getLogoPath(this, 0));
        }

        if(mCollection != null){
            final RelativeLayout collectionRelativeLayout = (RelativeLayout) findViewById(R.id.movie_collection_relativelayout);
            collectionRelativeLayout.setVisibility(View.VISIBLE);

//            TextView companyTextView = (TextView) findViewById(R.id.movie_productioncompany_textview);
//            companyTextView.setText(mCompanyList.get(0).getName());
            final ImageView collectionImageView = (ImageView) findViewById(R.id.movie_collection_imageview);

            collectionImageView.post(new Runnable() {
                @Override
                public void run() {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) collectionImageView.getLayoutParams();
                    params.width = collectionRelativeLayout.getWidth();
                    params.height = collectionRelativeLayout.getWidth() / 2;
                }
            });
            Picasso.with(this)
                    .load(mCollection.getBackdropPath(this, 0))
                    .error(R.drawable.no_image_land)
                    .resize(collectionRelativeLayout.getWidth(), collectionRelativeLayout.getWidth()/2)
                    .centerCrop()
                    .into(collectionImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            findViewById(R.id.movie_collection_progressbar).setVisibility(View.GONE);
                            TextView collectionTextView = (TextView) findViewById(R.id.movie_collection_textview);
                            collectionTextView.setVisibility(View.VISIBLE);
                            collectionTextView.setText(mCollection.getName());
                        }

                        @Override
                        public void onError() {

                        }
                    });
        }

        if(mKeywordList.size() > 0){
            TagContainerLayout genreContainer = (TagContainerLayout) findViewById(R.id.keyword_tagcontainer);
            genreContainer.setVisibility(View.VISIBLE);
            String[] tags = new String[mKeywordList.size()];
            for(int i = 0; i < mKeywordList.size(); i++){
                tags[i] = mKeywordList.get(i).getName();
            }
            genreContainer.setTags(tags);
            genreContainer.setOnTagClickListener(new TagView.OnTagClickListener() {
                @Override
                public void onTagClick(int position, String text) {
                    Keyword keyword = mKeywordList.get(position);
                    Log.e("keyword clicked", keyword.getId()+" - "+keyword.getName());
                }

                @Override
                public void onTagLongClick(int position, String text) {

                }
            });
        }
    }

    public static int getColorWithAlpha(float alpha, int baseColor) {
        int a = Math.min(255, Math.max(0, (int) (alpha * 255))) << 24;
        int rgb = 0x00ffffff & baseColor;
        return a + rgb;
    }
}