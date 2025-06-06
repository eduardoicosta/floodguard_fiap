package com.enzop.floodguard

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var spinnerLocation: Spinner
    private lateinit var buttonRegister: Button
    private lateinit var textViewLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicialização dos componentes da UI
        editTextName = findViewById(R.id.editTextName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        spinnerLocation = findViewById(R.id.spinnerLocation)
        buttonRegister = findViewById(R.id.buttonRegister)
        textViewLogin = findViewById(R.id.textViewLogin)

        // Configurar o Spinner com algumas localizações (baseado em listaPontosAlagamento)
        val locations = listOf(
            "Selecione uma localização",
            "Recife, Brasil",
            "Veneza, Itália",
            "New Orleans, EUA",
            "Jakarta, Indonésia",
            "Mumbai, Índia"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, locations)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLocation.adapter = adapter

        // Ação do botão de Cadastro
        buttonRegister.setOnClickListener {
            val name = editTextName.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val location = spinnerLocation.selectedItem.toString()

            if (validateInput(name, email, password, location)) {
                // Exemplo de validação estática (substituir por backend no futuro)
                Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                // Redireciona para LoginActivity
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish() // Finaliza RegisterActivity
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos corretamente.", Toast.LENGTH_SHORT).show()
            }
        }

        // Ação do texto de Voltar ao Login
        textViewLogin.setOnClickListener {
            finish() // Volta para LoginActivity
        }
    }

    private fun validateInput(name: String, email: String, password: String, location: String): Boolean {
        return when {
            name.isEmpty() -> false
            email.isEmpty() || !email.contains("@") -> false
            password.length < 6 -> {
                Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres.", Toast.LENGTH_SHORT).show()
                false
            }
            location == "Selecione uma localização" -> false
            else -> true
        }
    }
}