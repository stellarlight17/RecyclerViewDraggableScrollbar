# Recycler View Draggable Scrollbar

### A customerizable scrollbar for recycler view that:
1. solves thumb size becoming too small problem when dealing with large list
2. supports fast scrolling

### Usage:
1. Declare the scrollbar in XML together with your recycler view, for example:
```xml
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scrollbars="none" />

    <com.stellarlight17.DraggableScrollbarView
        android:id="@+id/scrollbarView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:trackWidth="@dimen/scrollbar_track_width"
        app:trackColor="@android:color/transparent"
        app:thumbWidth="@dimen/scrollbar_track_width"
        app:thumbHeight="@dimen/scrollbar_thumb_height"
        app:thumbColor="@color/purple_500"
        app:thumbCornerRadius="@dimen/scrollbar_thumb_corner_radius" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

2. Attach the scrollbar to your recycler view
```kotlin
scrollbarView.attachToRecyclerView(recyclerView)
```
