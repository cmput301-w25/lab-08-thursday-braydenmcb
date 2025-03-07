package com.example.androidcicd;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.androidcicd.movie.Movie;
import com.example.androidcicd.movie.MovieProvider;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;


import java.util.Collections;

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


    @Test
    public void testCheckTitleExistsShouldThrowErrorForDuplicateTitle() {
        QuerySnapshot mockQuerySnapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot mockDocumentSnapshot = mock(QueryDocumentSnapshot.class);

        // simulate an existing movie but with duplicate title
        when(mockDocumentSnapshot.getId()).thenReturn("456");
        when(mockQuerySnapshot.getDocuments()).thenReturn(Collections.singletonList(mockDocumentSnapshot));

        Task<QuerySnapshot> mockTask = mock(Task.class);
        when(mockTask.isSuccessful()).thenReturn(true);
        when(mockTask.getResult()).thenReturn(mockQuerySnapshot);

        when(mockMovieCollection.whereEqualTo("title", "Oppenheimer"))
                .thenReturn(mockMovieCollection);
        when(mockMovieCollection.get()).thenReturn(mockTask);

        // simulate calling checkTitleExists and get the callback
        movieProvider.checkTitleExists("Oppenheimer", "123", new MovieProvider.TitleValidatorCallback() {
            @Override
            public void onTitleValidated(boolean isUnique) {
                Assert.fail("Expected a duplicate title but got valid response instead.");
            }

            @Override
            public void onError(String error) {
                assertEquals("Error checking for duplicate titles", error);
            }
        });
    }

}
