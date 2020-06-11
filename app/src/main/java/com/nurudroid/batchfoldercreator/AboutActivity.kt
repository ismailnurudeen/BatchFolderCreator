package com.nurudroid.batchfoldercreator

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.vansuita.materialabout.builder.AboutBuilder
import kotlinx.android.synthetic.main.activity_about.*

/*
 *****************************************************
 * Created by Ismail Nurudeen on 08-Jun-20 at 9:14 AM.   *
 * Copyright (c) 2020 Nurudroid. All rights reserved. *
 ******************************************************
 **/
class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        loadAbout()
        about_nav_back.setOnClickListener {
            finish()
        }
    }

    private fun loadAbout() {
        val theme = R.style.AppThemeLight
        this.setTheme(theme)
        val flHolder: FrameLayout = this.findViewById(R.id.about_frame)
        val builder: AboutBuilder = AboutBuilder.with(this)
            .addWhatsappLink(
                this.getString(R.string.my_name),
                this.getString(R.string.my_phone_num)
            )
            .addFacebookLink(this.getString(R.string.my_fb_id))
            .addTwitterLink(this.getString(R.string.my_twitter_id))
            .addFiveStarsAction()
            .addUpdateAction()
            .addMoreFromMeAction(getString(R.string.playstore_username))
            .addShareAction(getString(R.string.share_app))
            .addFeedbackAction(R.string.my_email)
            .addLinkedInLink(this.getString(R.string.my_linkedin_id))
            .setVersionNameAsAppSubTitle()
            .setLinksAnimated(false)
            .setDividerDashGap(13)
            .setName(this.getString(R.string.my_name))
            .setSubTitle(getString(R.string.about_subtitle))
            .setLinksColumnsCount(4)
            .setBrief(this.getString(R.string.about_me))
            .setPhoto(R.mipmap.app_icon)
            .setCover(R.drawable.tech_bg)
            .setAppName(R.string.app_name)
            .setVersionNameAsAppSubTitle()
            .setActionsColumnsCount(2)

            .setWrapScrollView(true)
            .setShowAsCard(false)

        val view = builder.build()
        flHolder.addView(view)
    }
}