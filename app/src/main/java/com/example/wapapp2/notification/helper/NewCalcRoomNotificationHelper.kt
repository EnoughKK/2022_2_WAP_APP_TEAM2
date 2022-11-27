package com.example.wapapp2.notification.helper

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.wapapp2.R
import com.example.wapapp2.commons.classes.DeviceUtils
import com.example.wapapp2.model.notifications.NotificationObj
import com.example.wapapp2.model.notifications.NotificationType
import com.example.wapapp2.model.notifications.send.SendFcmCalcRoomDTO
import com.example.wapapp2.model.notifications.send.SendFcmReceiptDTO
import com.example.wapapp2.repository.ReceiptImgRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.joda.time.DateTime

class NewCalcRoomNotificationHelper private constructor(context: Context) :
        AbstractNotificationHelper(context, NotificationType.NewCalcRoom.channelId, context.getString(R.string
                .new_calc_room_notification_channel_name),
                context.getString(R.string.new_calc_room_notification_channel_description)) {


    companion object {
        private var INSTANCE: NewCalcRoomNotificationHelper? = null

        fun getINSTANCE(context: Context): NewCalcRoomNotificationHelper {
            if (INSTANCE == null)
                INSTANCE = NewCalcRoomNotificationHelper(context)
            return INSTANCE!!
        }
    }


    override fun createNotification(context: Context): NotificationObj {
        val notificationObj = super.createNotification(context)
        notificationObj.notificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
        notificationObj.notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        notificationObj.notificationBuilder.setAutoCancel(true)

        return notificationObj
    }

    fun notifyNotification(context: Context, sendFcmCalcRoomDTO: SendFcmCalcRoomDTO) {
        val notificationObj = createNotification(context)

        notificationObj.notificationBuilder.setContentTitle(context.getString(R.string.new_calc_room))
        notificationObj.notificationBuilder.setContentText(context.getString(R.string.invited_to_new_calc_room))

        val arguments = Bundle().apply {
            putParcelable("notificationType", NotificationType.NewCalcRoom)
            putString("calcRoomId", sendFcmCalcRoomDTO.roomId)
        }

        notificationObj.notificationBuilder.setContentIntent(createActivityIntent(context, arguments))
        notifyNotification(notificationObj)

        DeviceUtils.wakeLock(context)
    }


}