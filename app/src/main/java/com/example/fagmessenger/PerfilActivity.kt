package com.example.fagmessenger

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.fagmessenger.databinding.ActivityPerfilBinding
import com.example.fagmessenger.util.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage


class PerfilActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityPerfilBinding.inflate(layoutInflater)
    }
    private var temPermissaoCamera = false
    private var temPermissaoGaleria = false

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val storage by lazy {
        FirebaseStorage.getInstance()
    }

    private val gerenciadorGaleria = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            binding.imagePerfil.setImageURI(uri)
            uploadImagemStorage(uri)
        } else {
            exibirMensagem("Nenhuma imagem selecionada")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        inicializarToolbar()
        solicitarPermissoes()
        inicializarEventosClique()
    }
    //update da imagem
    private fun uploadImagemStorage(uri: Uri) {
        val idUsuario = firebaseAuth.currentUser?.uid

        if (idUsuario != null) {
            storage
                .getReference("fotos")
                .child("usuarios")
                .child(idUsuario)
                .child("perfil.jpg")
                .putFile(uri)
                .addOnSuccessListener { task ->

                    exibirMensagem("Sucesso ao fazer upload da imagem")

                }
                .addOnFailureListener {
                    exibirMensagem("Erro ao fazer upload da imagem")
                }
        }
    }


    private fun inicializarEventosClique() {
        binding.fabSelecionar.setOnClickListener {
            if (temPermissaoGaleria) {
                gerenciadorGaleria.launch("image/*")
            } else {
                exibirMensagem("Não tem permissão para acessar galeria")
                solicitarPermissoes()
            }
        }
    }

    private fun solicitarPermissoes() {

        //Verifico se usuário já tem permissão
        temPermissaoCamera = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        temPermissaoGaleria = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED

        //LISTA DE PERMISSÕES NEGADAS
        val listaPermissoesNegadas = mutableListOf<String>()
        if (!temPermissaoCamera)
            listaPermissoesNegadas.add(Manifest.permission.CAMERA)
        if (!temPermissaoGaleria)
            listaPermissoesNegadas.add(Manifest.permission.READ_MEDIA_IMAGES)

        if (listaPermissoesNegadas.isNotEmpty()) {

            //Solicitar multiplas permissões
            val gerenciadorPermissoes = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissoes ->

                temPermissaoCamera = permissoes[Manifest.permission.CAMERA]
                    ?: temPermissaoCamera

                temPermissaoGaleria = permissoes[Manifest.permission.READ_MEDIA_IMAGES]
                    ?: temPermissaoGaleria

            }
            gerenciadorPermissoes.launch(listaPermissoesNegadas.toTypedArray())

        }

    }


    private fun inicializarToolbar() {
        val toolbar = binding.includeToolbarPerfil.tbToolbar
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Editar perfil"
            setDisplayHomeAsUpEnabled(true)
        }
    }
}
