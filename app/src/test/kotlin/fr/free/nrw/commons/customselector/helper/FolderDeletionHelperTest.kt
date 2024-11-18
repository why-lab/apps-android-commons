package fr.free.nrw.commons.customselector.helper

import android.content.Context
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.test.core.app.ApplicationProvider
import fr.free.nrw.commons.R
import fr.free.nrw.commons.TestCommonsApplication
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowToast
import java.io.File


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.R], application = TestCommonsApplication::class)
class FolderDeletionHelperTest {

    private lateinit var context: Context
    private lateinit var trashFolderLauncher: ActivityResultLauncher<IntentSenderRequest>

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        trashFolderLauncher = mock(ActivityResultLauncher::class.java) as ActivityResultLauncher<IntentSenderRequest>


    }



    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun `test confirmAndDeleteFolder calls deleteFolderMain on API 30 or higher`() {
        val folder = File("testFolder2")
        folder.mkdir()
        FolderDeletionHelper.confirmAndDeleteFolder(context, folder, trashFolderLauncher) { success ->
            //return false on empty folder
            assertFalse(success)
        }
        //verify if deleteFolderMain was called correctly without triggering an AlertDialog on API 30+
        val alertDialog = ShadowAlertDialog.getLatestAlertDialog()
        assertNull(alertDialog)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `test deleteFolderMain calls deleteFolderLegacy on API 29 or lower`() {
        val folder = File("testFolder3")
        val success = FolderDeletionHelper.deleteFolderMain(context, folder, trashFolderLauncher)
        assertEquals(success, folder.deleteRecursively())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun `test deleteFolderMain calls trashFolderContents on API 30 or higher`() {
        val folder = File("testFolder4")
        folder.mkdir()
        val success = FolderDeletionHelper.deleteFolderMain(context, folder, trashFolderLauncher)
        //return false for empty folder
        assertFalse(success)
    }

    @Test
    fun `test countItemsInFolder returns correct item count`() {
        val folder = File("testFolder5")
        folder.mkdir()
        val itemCount = FolderDeletionHelper.countItemsInFolder(context, folder)
        assertEquals(0, itemCount)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `test trashFolderContents returns false on API 29 or lower`() {
        val folder = File("testFolder6")
        folder.mkdir()
        val success = FolderDeletionHelper.trashFolderContents(context, folder, trashFolderLauncher)
        assertFalse(success)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun `test trashFolderContents fails with empty folder`() {
        val folder = File("testFolder7")
        val success = FolderDeletionHelper.trashFolderContents(context, folder, trashFolderLauncher)
        //folder is empty, will return false
        assertFalse(success)
    }


    @Test
    fun `test getFolderPath returns correct path`() {
        val folderId = 12345L
        val folderPath = FolderDeletionHelper.getFolderPath(context, folderId)

        //since there’s no actual data in the ContentResolver, it returns null.
        assertNull(folderPath)
    }

    @Test
    fun `test showError displays error toast and logs message`() {
        val folderName = "testFolder"
        val message = "Failed to delete folder"
        FolderDeletionHelper.showError(context, message, folderName)

        // Verify the toast message
        val toastMessage = context.getString(R.string.custom_selector_folder_deleted_failure, folderName)
        assertEquals(toastMessage, ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `test showSuccess displays success toast and logs message`() {
        val folderName = "testFolder"
        val message = "Folder deleted successfully"
        FolderDeletionHelper.showSuccess(context, message, folderName)

        // Verify the toast message
        val toastMessage = context.getString(R.string.custom_selector_folder_deleted_success, folderName)
        assertEquals(toastMessage, ShadowToast.getTextOfLatestToast())
    }
}
