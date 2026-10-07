package com.olam.digital.crs.fragment

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.olam.digital.crs.R
import com.olam.digital.crs.databinding.LayoutCrsConsentsBinding
import com.olam.digital.crs.extension.PdfPageAdapter
import com.olam.digital.crs.extension.longToast
import com.olam.digital.crs.extension.postNoData
import com.olam.digital.crs.model.ContentType
import com.olam.digital.crs.model.ResourceState
import com.olam.digital.crs.model.mapper.FarmerModuleView
import com.olam.digital.crs.viewmodel.CrsQuestionaryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.hwpf.usermodel.PictureType
import org.apache.poi.hwpf.usermodel.TableIterator
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFTable
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipFile

class CrsConsentFragment : Fragment() {
    lateinit var binding: LayoutCrsConsentsBinding
    private val crsViewModel: CrsQuestionaryViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LayoutCrsConsentsBinding.inflate(inflater, container, false)
        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.apply {
            setUi(null)
            setObservers()
        }
    }

    fun displayInWebView(file: File) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.VISIBLE
                }

                val sb = StringBuilder("""
                <html>
                <head>
                  <style>
                    body { color:#4A5565; line-height:1.5; font-size:14px; }
                    h1 { font-size:22px; font-weight:bold; margin:16px 0 8px; }
                    h2 { font-size:18px; font-weight:bold; margin:14px 0 6px; }
                    h3 { font-size:16px; font-weight:bold; margin:12px 0 4px; }
                    table { border-collapse: collapse; width: 100%; margin: 12px 0; }
                    th, td { border: 1px solid #444; padding: 6px; text-align: left; }
                    th { background-color: #f0f0f0; }
                    ul { margin: 8px 0; padding-left: 20px; list-style-type: disc; }
                    li { margin-bottom: 4px; }
                    a { color:#1a73e8; text-decoration: underline; }
                  </style>
                </head>
                <body>
            """.trimIndent())

                if (file.extension.equals("docx", ignoreCase = true)) {
                    FileInputStream(file).use { stream ->
                        XWPFDocument(stream).use { document ->
                            var inList = false
                            document.bodyElements.forEach { element ->
                                when (element) {
                                    is XWPFParagraph -> {
                                        val text = element.text.trim()
                                        if (text.isNotEmpty()) {
                                            val style = element.style ?: ""
                                            val linked = linkifyText(escapeHtml(text))
                                            when {
                                                style.equals("Heading1", true) ->
                                                    sb.append("<h1>").append(linked).append("</h1>")
                                                style.equals("Heading2", true) ->
                                                    sb.append("<h2>").append(linked).append("</h2>")
                                                style.equals("Heading3", true) ->
                                                    sb.append("<h3>").append(linked).append("</h3>")
                                                element.numIlvl != null -> { // list detection
                                                    if (!inList) { sb.append("<ul>"); inList = true }
                                                    sb.append("<li>").append(linked).append("</li>")
                                                }
                                                else -> {
                                                    if (inList) { sb.append("</ul>"); inList = false }
                                                    sb.append("<p>").append(linked).append("</p>")
                                                }
                                            }
                                        }
                                    }
                                    is XWPFTable -> {
                                        if (inList) { sb.append("</ul>"); inList = false }
                                        val rows = element.rows.map { row -> row.tableCells.map { it.text.trim() } }
                                        sb.append(renderTable(rows))
                                    }
                                }
                            }
                            if (inList) sb.append("</ul>")

                            // Images
                            document.allPictures.forEach { pic ->
                                val mimeType = detectMimeType(pic.data)
                                val base64 = Base64.encodeToString(pic.data, Base64.DEFAULT)
                                sb.append("<img src='data:$mimeType;base64,$base64' style='max-width:100%;'/><br>")
                            }
                        }
                    }
                } else if (file.extension.equals("doc", ignoreCase = true)) {
                    FileInputStream(file).use { stream ->
                        HWPFDocument(stream).use { doc ->
                            val range = doc.range
                            var i = 0
                            var inList = false
                            while (i < range.numParagraphs()) {
                                val para = range.getParagraph(i)
                                val text = para.text().trim()
                                if (para.isInTable) {
                                    val table = range.getTable(para)
                                    val rows = (0 until table.numRows()).map { rowIndex ->
                                        val row = table.getRow(rowIndex)
                                        (0 until row.numCells()).map { cellIndex ->
                                            val cell = row.getCell(cellIndex)
                                            (0 until cell.numParagraphs())
                                                .joinToString(" ") { cell.getParagraph(it).text().trim() }
                                        }
                                    }
                                    sb.append(renderTable(rows))
                                    i += table.numParagraphs()
                                    if (inList) { sb.append("</ul>"); inList = false }
                                } else {
                                    if (text.isNotEmpty()) {
                                        val linked = linkifyText(escapeHtml(text))
                                        when (para.styleIndex.toInt()) {
                                            1 -> sb.append("<h1>").append(linked).append("</h1>")
                                            2 -> sb.append("<h2>").append(linked).append("</h2>")
                                            3 -> sb.append("<h3>").append(linked).append("</h3>")
                                            else -> if (para.isInList) { // HWPF list detection
                                                if (!inList) { sb.append("<ul>"); inList = true }
                                                sb.append("<li>").append(linked).append("</li>")
                                            } else {
                                                if (inList) { sb.append("</ul>"); inList = false }
                                                sb.append("<p>").append(linked).append("</p>")
                                            }
                                        }
                                    }
                                    i++
                                }
                            }
                            if (inList) sb.append("</ul>")

                            // Images
                            doc.picturesTable.allPictures.forEach { pic ->
                                val base64 = Base64.encodeToString(pic.content, Base64.DEFAULT)
                                sb.append("<img src='data:image/png;base64,$base64' style='max-width:100%;'/><br>")
                            }
                        }
                    }
                }

                sb.append("</body></html>")

                withContext(Dispatchers.Main) {
                    binding.docWebView.loadDataWithBaseURL("file://", sb.toString(), "text/html", "UTF-8", null)
                    binding.progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                Timber.e(e, "Error displaying document in WebView ${e.message}")
                withContext(Dispatchers.Main) {
                    binding.docWebView.loadData("<p>Error loading document</p>", "text/html", "UTF-8")
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    // Escape HTML special characters
    fun escapeHtml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    // Render table rows into HTML
    fun renderTable(rows: List<List<String>>): String {
        val sb = StringBuilder("<table>")
        rows.forEachIndexed { rowIndex, row ->
            sb.append("<tr>")
            row.forEach { cellText ->
                val safeText = if (cellText.isBlank()) "&nbsp;" else linkifyText(escapeHtml(cellText))
                if (rowIndex == 0) {
                    sb.append("<th>").append(safeText).append("</th>")
                } else {
                    sb.append("<td>").append(safeText).append("</td>")
                }
            }
            sb.append("</tr>")
        }
        sb.append("</table><br>")
        return sb.toString()
    }

    // Detect MIME type from image data using your PictureType enum
    fun detectMimeType(data: ByteArray): String {
        val type = PictureType.findMatchingType(data)
        return type.mime
    }


    // Convert URLs in text into clickable links
    fun linkifyText(text: String): String {
        val urlRegex = "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+|www\\.[\\w\\-._~:/?#@!$&'()*+,;=%]+)".toRegex()
        val emailRegex = "[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+".toRegex()

        var result = text.replace(urlRegex) { match ->
            val url = match.value
            "<a href=\"$url\" target=\"_blank\">$url</a>"
        }
        result = result.replace(emailRegex) { match ->
            val email = match.value
            "<a href=\"mailto:$email\">$email</a>"
        }
        return result
    }







    fun loadConsentContent(type: ContentType, source: Any) {
        when (type) {
            ContentType.TEXT -> {
                binding.action = ContentType.TEXT
                binding.txtSurveyContent.text = source.toString()
            }

            ContentType.DOCUMENT -> {
                binding.action = ContentType.DOCUMENT

                val file = when (source) {
                    is File -> source
                    is String -> File(source)
                    else -> File("") // safe fallback
                }
                if (file.exists()) {
                    displayInWebView(file)
                } else {
                    binding.action = ContentType.TEXT
                    Timber.e("Document file does not exist at path: ${file.absolutePath}")
                    binding.txtSurveyContent.text = "Unable to load document content."
                }
            }

            ContentType.IMAGE -> {
                binding.action = ContentType.IMAGE

                Glide.with(binding.pdfImageView.context)
                    .load(source) // File or URL
                    .into(binding.pdfImageView)
            }

            ContentType.PDF -> {
                binding.action = ContentType.PDF


                val file = when (source) {
                    is File -> source
                    is String -> File(source)
                    else -> File("") // safe fallback
                }
                if (file.exists()) {
                    val fileDescriptor =
                        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                    val renderer = PdfRenderer(fileDescriptor)
                    binding.pdfRecyclerView.layoutManager =
                        LinearLayoutManager(binding.root.context)
                    binding.pdfRecyclerView.adapter = PdfPageAdapter(renderer)
                } else {
                    Timber.e("PDF file does not exist at path: ${file.absolutePath}")
                    binding.action = ContentType.TEXT
                    binding.txtSurveyContent.text = "Unable to load PDF content."
                }
            }
        }
    }


    fun mapConsentType(consentType: String?, consentPath: String?): ContentType {
        return when (consentType?.lowercase()) {
            "text" -> ContentType.TEXT
            "image" -> ContentType.IMAGE
            "file" -> {
                when (consentPath?.substringAfterLast(".")?.lowercase()) {
                    "pdf" -> ContentType.PDF
                    "doc", "docx" -> ContentType.DOCUMENT
                    "jpg", "jpeg", "png" -> ContentType.IMAGE
                    else -> ContentType.TEXT
                }
            }

            else -> ContentType.TEXT
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setUi(lData: FarmerModuleView?) {
        binding.apply {
            crsViewModel.apply {
                lData.apply {
                    txtSurveyValue.text = lData?.name?.toString() ?: "N/A"
                    txtRespondentValue.text =
                        if (crsViewModel.isCrsFragment.value == false) crsViewModel.selectedUser.value?.data?.olamFarmerId
                            ?: "N/A"
                        else crsViewModel.selectedCrsProfiles.value?.data?.respondentCode ?: "N/A"
                    txtSurveyContent.text = lData?.consentText ?: "N/A"

                    //here i need to add logic to load content based on type
                    val type = mapConsentType(lData?.consentType, lData?.consentPath)

                    val source: Any = when (type) {
                        ContentType.TEXT -> lData?.consentText ?: "N/A"
                        ContentType.DOCUMENT,
                        ContentType.IMAGE,
                        ContentType.PDF -> {
                            val path = lData?.consentText
                            if (!path.isNullOrEmpty()) {
                                val file = File(path)
                                if (file.exists()) file else File("") // safe fallback
                            } else File("")
                        }
                    }
                    Timber.d("Loading consent content of type $type from source: $source")

                    loadConsentContent(type, source)


                    loadConsentContent(
                        mapConsentType(lData?.consentType, lData?.consentPath),
                        lData?.consentText ?: ""
                    )

                    txtSurveyContent.movementMethod = android.text.method.ScrollingMovementMethod()
                    imgBack.setOnClickListener {
                        selectedModule.postNoData()
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                    txtClear.setOnClickListener {
                        signaturePad.clear()
                    }


                    txtSurveyContent.setOnTouchListener { v, event ->
                        v.parent.requestDisallowInterceptTouchEvent(true)
                        false
                    }


                    chkBox.setOnCheckedChangeListener { btn, isChecked ->
                        if (isChecked) {
                            btn.alpha = 1.0f
                        } else {
                            btn.alpha = 0.5f
                        }
                    }
                    layBottom.setOnClickListener {
                        if (!chkBox.isChecked || signaturePad.isEmpty) {
                            chkBox.context.longToast(getString(R.string.please_fill_all_fields))
                            return@setOnClickListener
                        }

                        crsViewModel.selectedCrsContentImage.postValue(saveToGalleryAndGetUri())
                        /* requireActivity().supportFragmentManager.beginTransaction()
                             .replace(
                                 R.id.fragmentContainerView,
                                 SurveyFragment()
                             )
                             .addToBackStack(null)
                             .commit()*/
                        val navController =
                            requireActivity().findNavController(R.id.fragmentContainerView)
                        navController.navigate(R.id.action_crsConsentFragment_to_surveyFragment)

                    }
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveToGalleryAndGetUri(): Uri {
        val bitmap = binding.signaturePad.signatureBitmap
        val fileName = "signature_${System.currentTimeMillis()}.png"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Signatures")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val uri = context?.contentResolver
            ?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!

        val stream = context?.contentResolver?.openOutputStream(uri)
        stream?.let {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
        }

        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        context?.contentResolver?.update(uri, values, null, null)
        return uri
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setObservers() {
        binding.apply {
            crsViewModel.apply {
                selectedModule.observe(viewLifecycleOwner)
                {
                    when (it.state) {
                        ResourceState.NO_DATA -> {

                        }

                        ResourceState.ERROR -> {

                        }

                        ResourceState.LOADING -> {

                        }

                        ResourceState.SUCCESS -> {
                            setUi(it.data)
                        }
                    }
                }
            }
        }
    }


}