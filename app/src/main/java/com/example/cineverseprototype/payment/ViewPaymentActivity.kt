package com.example.cineverseprototype.payment

import android.R.attr.label
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.cineverseprototype.Singleton
import com.example.cineverseprototype.ToastUtil
import com.example.cineverseprototype.databinding.ActivityViewPaymentBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter


class ViewPaymentActivity : AppCompatActivity() {

    private lateinit var binding:ActivityViewPaymentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.progress.hide()
        hideQRCode()

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Error Occurred")
        dialogBuilder.setMessage("Unable to retrieve the information you requested. Please try again later.")
        dialogBuilder.setPositiveButton("Exit") { dialog, _ ->
            dialog.dismiss()
        }
        dialogBuilder.setOnDismissListener {
            finish()
        }

        val errorDialog = dialogBuilder.create()

        val payment = intent.getSerializableExtra("payment") as? Payment
        if(payment != null){
            binding.movieName.text = payment.movieName
            binding.branchName.text = payment.getBranchInfo()
            binding.theatreType.text = payment.getSeatInfo()
            binding.scheduleTime.text = payment.getScheduleTime()
            binding.scheduleDate.text = payment.getScheduleDate()

            binding.statusValue.text = payment.paymentStatus
            binding.statusValue.setTextColor(Color.parseColor(payment.getStatusColor()))

            binding.dateLabel.text = payment.getDateLabel()
            binding.dateValue.text = payment.getLatestDate()
            binding.ticketPrice.text = String.format("RM %.2f",payment.totalPrice)

            if(payment.showMethod()){
                binding.payMethodContainer.visibility = View.VISIBLE
                binding.paidMethod.text = payment.paymentType

                binding.qrCode.setImageBitmap(generateQRCode(payment.paymentId))
                showQRCode()
            }
            else{
                binding.payMethodContainer.visibility = View.GONE

                hideQRCode()
            }

            binding.copyIdBtn.setOnClickListener {
                val clipboard: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("ReferenceId", payment.paymentId)
                clipboard.setPrimaryClip(clip)

                ToastUtil.initializeToast(applicationContext,"Unexpected error occurred. Please try again later.",
                    Toast.LENGTH_LONG).show()
            }
        }
        else{
            errorDialog.show()
        }
    }

    private fun generateQRCode(transactionId:String):Bitmap{
        val size = 512 //pixels
        val hints = hashMapOf<EncodeHintType, Int>().also { it[EncodeHintType.MARGIN] = 1 } // Make the QR code buffer border narrower
        val bits = QRCodeWriter().encode(transactionId, BarcodeFormat.QR_CODE, size, size, hints)
        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }

    private fun hideQRCode(){
        binding.emptyContainer.visibility = View.VISIBLE
        binding.qrCode.visibility = View.GONE
    }

    private fun showQRCode(){
        binding.emptyContainer.visibility = View.GONE
        binding.qrCode.visibility = View.VISIBLE
    }
}