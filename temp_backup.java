<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/background_primary"
    tools:context=".AttendanceActivity">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        <!-- Header Card -->
        <androidx.cardview.widget.CardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_margin="0dp"
            app:cardBackgroundColor="@color/unacademy_green"
            app:cardCornerRadius="0dp"
            app:cardElevation="4dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="24dp"
                android:paddingTop="48dp">

                <!-- Class Name -->
                <TextView
                    android:id="@+id/classNameText"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="Computer Science - 1st - A"
                    android:textSize="24sp"
                    android:textStyle="bold"
                    android:textAlignment="center"
                    android:textColor="@android:color/white"
                    android:fontFamily="sans-serif-medium"
                    android:layout_marginBottom="8dp" />

                <!-- Subject Info -->
                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="Mark attendance for today"
                    android:textSize="16sp"
                    android:textAlignment="center"
                    android:textColor="#E8F8F5"
                    android:fontFamily="sans-serif" />

            </LinearLayout>

        </androidx.cardview.widget.CardView>

        <!-- Main Content Container -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="20dp">

            <!-- Sort Button Card -->
            <androidx.cardview.widget.CardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="24dp"
                app:cardBackgroundColor="@color/background_card"
                app:cardCornerRadius="12dp"
                app:cardElevation="2dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:padding="16dp"
                    android:gravity="center_vertical">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="Student Order"
                        android:textSize="16sp"
                        android:textColor="@color/text_primary"
                        android:fontFamily="sans-serif-medium" />

                    <Button
                        android:id="@+id/sortButton"
                        android:layout_width="wrap_content"
                        android:layout_height="48dp"
                        android:text="CSV Order"
                        android:textSize="14sp"
                        android:textColor="@color/unacademy_green"
                        android:background="@drawable/button_sort_background"
                        android:paddingHorizontal="20dp"
                        android:paddingVertical="12dp"
                        android:fontFamily="sans-serif-medium"
                        android:minWidth="120dp" />

                </LinearLayout>

            </androidx.cardview.widget.CardView>

            <!-- Student Card -->
            <androidx.cardview.widget.CardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="24dp"
                app:cardBackgroundColor="@color/background_card"
                app:cardCornerRadius="16dp"
                app:cardElevation="4dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="32dp"
                    android:gravity="center">

                    <!-- Student Name -->
                    <TextView
                        android:id="@+id/studentNameText"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="Student Name"
                        android:textSize="28sp"
                        android:textAlignment="center"
                        android:textColor="@color/text_primary"
                        android:textStyle="bold"
                        android:fontFamily="sans-serif-medium"
                        android:layout_marginBottom="8dp" />

                    <!-- Enrollment Number -->
                    <TextView
                        android:id="@+id/studentEnrollmentText"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="Enrollment No."
                        android:textSize="16sp"
                        android:textAlignment="center"
                        android:textColor="@color/text_secondary"
                        android:fontFamily="sans-serif"
                        android:layout_marginBottom="16dp" />

                    <!-- Counter -->
                    <LinearLayout
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:background="@drawable/counter_background"
                        android:paddingHorizontal="16dp"
                        android:paddingVertical="8dp"
                        android:layout_marginBottom="32dp">

                        <TextView
                            android:id="@+id/counterText"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="1 / 30"
                            android:textSize="16sp"
                            android:textColor="@color/unacademy_green"
                            android:fontFamily="sans-serif-medium" />

                    </LinearLayout>

                    <!-- Attendance Buttons -->
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal"
                        android:gravity="center"
                        android:layout_marginBottom="24dp">

                        <Button
                            android:id="@+id/presentButton"
                            android:layout_width="120dp"
                            android:layout_height="120dp"
                            android:layout_marginEnd="24dp"
                            android:text="P"
                            android:textSize="42sp"
                            android:textStyle="bold"
                            android:background="@drawable/present_button_selector"
                            android:textColor="@android:color/white"
                            android:fontFamily="sans-serif-medium"
                            android:backgroundTint="@null"
                            style="@android:style/Widget.Button" />

                        <Button
                            android:id="@+id/absentButton"
                            android:layout_width="120dp"
                            android:layout_height="120dp"
                            android:text="A"
                            android:textSize="42sp"
                            android:textStyle="bold"
                            android:background="@drawable/absent_button_selector"
                            android:textColor="@android:color/white"
                            android:fontFamily="sans-serif-medium"
                            android:backgroundTint="@null"
                            style="@android:style/Widget.Button" />
                            android:elevation="6dp"
                            android:fontFamily="sans-serif-medium" />

                    </LinearLayout>

                </LinearLayout>

            </androidx.cardview.widget.CardView>

            <!-- Navigation Card -->
            <androidx.cardview.widget.CardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="24dp"
                app:cardBackgroundColor="@color/background_card"
                app:cardCornerRadius="12dp"
                app:cardElevation="2dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:padding="16dp">

                    <Button
                        android:id="@+id/prevButton"
                        android:layout_width="0dp"
                        android:layout_height="56dp"
                        android:layout_weight="1"
                        android:layout_marginEnd="8dp"
                        android:text="Previous"
                        android:textSize="16sp"
                        android:textColor="@color/text_secondary"
                        android:background="@drawable/button_nav_background"
                        android:fontFamily="sans-serif-medium"
                        android:paddingVertical="16dp" />

                    <Button
                        android:id="@+id/nextButton"
                        android:layout_width="0dp"
                        android:layout_height="56dp"
                        android:layout_weight="1"
                        android:layout_marginStart="8dp"
                        android:text="Next"
                        android:textSize="16sp"
                        android:textColor="@color/unacademy_green"
                        android:background="@drawable/button_nav_primary_background"
                        android:fontFamily="sans-serif-medium"
                        android:paddingVertical="16dp" />

                </LinearLayout>

            </androidx.cardview.widget.CardView>

            <!-- Finish Button -->
            <Button
                android:id="@+id/finishButton"
                android:layout_width="match_parent"
                android:layout_height="64dp"
                android:text="Finish &amp; Generate Report"
                android:textSize="18sp"
                android:textColor="@android:color/white"
                android:background="@drawable/button_primary_background"
                android:fontFamily="sans-serif-medium"
                android:paddingVertical="20dp"
                android:layout_marginTop="8dp" />

        </LinearLayout>

    </LinearLayout>

</ScrollView>
