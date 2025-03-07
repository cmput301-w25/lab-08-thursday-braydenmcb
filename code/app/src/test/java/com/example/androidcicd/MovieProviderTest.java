package com.example.androidcicd;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.androidcicd.movie.Movie;
import com.example.androidcicd.movie.MovieProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class MovieProviderTest {
    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private CollectionReference mockMovieCollection;

    @Mock
    private DocumentReference mockDocReference;

    private MovieProvider movieProvider;

    @Before
    public void setup() {
        // Start up the mocks
        MockitoAnnotations.openMocks(this);

        // Define what we want our mocks to do in our tests
        when(mockFirestore.collection("movies")).thenReturn(mockMovieCollection);
        when(mockMovieCollection.document()).thenReturn(mockDocReference);
        when(mockMovieCollection.document(anyString())).thenReturn(mockDocReference);

        // make sure theres a fresh instance for each test
        MovieProvider.setInstanceForTesting(mockFirestore);
        movieProvider = MovieProvider.getInstance(mockFirestore);

    }

    @Test
    public void testAddMovieSetsId() {
        // movie to add
        Movie movie = new Movie("Oppenheimer", "Thriller/Historical Drama", 2023);

        // define the ID we want to set for the movie
        when(mockDocReference.getId()).thenReturn("123");

        // "add" movie to firestore
        movieProvider.addMovie(movie);
        assertEquals("Movie was not updated with the correct id", "123", movie.getId());

        // Verify we called set movie in firestore
        verify(mockDocReference).set(movie);
    }

    @Test
    public void testDeleteMovie() {
        Movie movie = new Movie("Oppenheimer", "Thriller/Historical Drama", 2023);
        movie.setId("123");

        // call delete movie
        movieProvider.deleteMovie(movie);
        verify(mockDocReference).delete();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateMovieShouldThrowErrorForDifferentId() {
        Movie movie = new Movie("Oppenheimer", "Thriller/Historical Drama", 2023);
        movie.setId("1");

        // Make sure the doc ref has different ID
        when(mockDocReference.getId()).thenReturn("123");

        // Call update
        movieProvider.updateMovie(movie, "Another title", "another Genre", 2007);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateMovieShouldThrowErrorForEmptyName() {
        Movie movie = new Movie("Oppenheimer", "Thriller/Historical Drama", 2023);
        movie.setId("123");
        when(mockDocReference.getId()).thenReturn("123");
        //Call update, which should throw error for empty name
        movieProvider.updateMovie(movie, "", "Another Genre", 2007);
    }
}
