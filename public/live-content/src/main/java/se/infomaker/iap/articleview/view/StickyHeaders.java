/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Doist
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package se.infomaker.iap.articleview.view;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Adds sticky headers capabilities to the {@link RecyclerView.Adapter}. Should return {@code true} for all
 * positions that represent sticky headers.
 *
 * Original copy: https://github.com/Doist/RecyclerViewExtensions/blob/master/StickyHeaders/src/main/java/io/doist/recyclerviewext/sticky_headers/StickyHeaders.java
 */
public interface StickyHeaders {
    boolean isStickyHeader(int position);

    interface ViewSetup {
        /**
         * Adjusts any necessary properties of the {@code holder} that is being used as a sticky header.
         *
         * {@link #teardownStickyHeaderView(View)} will be called sometime after this method
         * and before any other calls to this method go through.
         */
        void setupStickyHeaderView(View stickyHeader);

        /**
         * Reverts any properties changed in {@link #setupStickyHeaderView(View)}.
         *
         * Called after {@link #setupStickyHeaderView(View)}.
         */
        void teardownStickyHeaderView(View stickyHeader);
    }
}
