package com.enzop.floodguard

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView

// Data classes e Enum
data class Sensor(val id: String, val nome: String, val latitude: Double, val longitude: Double, val nivelAguaMetros: Double)
enum class TipoDeRiscoAlagamento { COMUM, ELEVADO, CRITICO }
data class PontoAlagamento(
    val id: String, val nomeLocal: String, val estado: String, val tipoRisco: TipoDeRiscoAlagamento,
    val descricaoBreve: String, val latitude: Double, val longitude: Double
)

class MainActivity : AppCompatActivity(), OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var textViewSensorName: TextView
    private lateinit var textViewSensorId: TextView
    private lateinit var textViewWaterLevel: TextView
    private lateinit var textViewStatus: TextView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private var isMapInDarkMode = false
    private lateinit var fabToggleMapTheme: FloatingActionButton
    private lateinit var fabMyLocation: FloatingActionButton
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permissão de localização concedida!", Toast.LENGTH_SHORT).show()
                getDeviceLocation()
            } else {
                Toast.makeText(this, "Permissão de localização negada.", Toast.LENGTH_LONG).show()
            }
        }

    // Lista de sensores locais (simulada)
    private val listaDeSensores = listOf(
        Sensor(id = "S001", nome = "Sensor Marg. Pinheiros", latitude = -23.57, longitude = -46.70, nivelAguaMetros = 1.2),
        Sensor(id = "S002", nome = "Sensor Rio Tietê", latitude = -23.52, longitude = -46.63, nivelAguaMetros = 2.8),
        Sensor(id = "S003", nome = "Sensor Tamanduateí", latitude = -23.56, longitude = -46.61, nivelAguaMetros = 4.1)
    )

    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    // NOVA LISTA EXPANDIDA COM PONTOS DE RISCO EM TODO O SUDESTE DO BRASIL
    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    private val listaPontosAlagamento = listOf(
        // São Paulo
        PontoAlagamento("SP01", "Franco da Rocha", "SP", TipoDeRiscoAlagamento.CRITICO, "Inundações recorrentes do Rio Juquery", -23.3286, -46.7258),
        PontoAlagamento("SP02", "São Sebastião", "SP", TipoDeRiscoAlagamento.CRITICO, "Risco de deslizamentos com chuvas intensas na Serra", -23.763, -45.412),
        PontoAlagamento("SP03", "Santos (Zona Noroeste)", "SP", TipoDeRiscoAlagamento.ELEVADO, "Inundação por maré alta e drenagem deficiente", -23.933, -46.38),
        PontoAlagamento("SP04", "Itapevi", "SP", TipoDeRiscoAlagamento.ELEVADO, "Enchentes do Rio Barueri-Mirim", -23.5489, -46.9342),
        PontoAlagamento("SP05", "Registro (Vale do Ribeira)", "SP", TipoDeRiscoAlagamento.ELEVADO, "Inundações extensas do Rio Ribeira de Iguape", -24.4886, -47.8442),
        PontoAlagamento("SP06", "Mogi das Cruzes", "SP", TipoDeRiscoAlagamento.COMUM, "Enchentes do curso superior do Rio Tietê", -23.52, -46.18),

        // Rio de Janeiro
        PontoAlagamento("RJ01", "Petrópolis (Reg. Serrana)", "RJ", TipoDeRiscoAlagamento.CRITICO, "Deslizamentos e inundações relâmpago severas", -22.5050, -43.1789),
        PontoAlagamento("RJ02", "Teresópolis (Reg. Serrana)", "RJ", TipoDeRiscoAlagamento.CRITICO, "Histórico de desastres por chuvas fortes", -22.4122, -42.9658),
        PontoAlagamento("RJ03", "Duque de Caxias (Baixada)", "RJ", TipoDeRiscoAlagamento.ELEVADO, "Alagamentos por chuvas e transbordamento de rios", -22.7858, -43.3114),
        PontoAlagamento("RJ04", "Angra dos Reis", "RJ", TipoDeRiscoAlagamento.ELEVADO, "Deslizamentos de terra em encostas", -23.0069, -44.3183),
        PontoAlagamento("RJ05", "Campos dos Goytacazes", "RJ", TipoDeRiscoAlagamento.ELEVADO, "Inundações do Rio Paraíba do Sul", -21.7522, -41.3256),
        PontoAlagamento("RJ06", "Rio de Janeiro (Jardim Botânico)", "RJ", TipoDeRiscoAlagamento.COMUM, "Alagamentos rápidos devido a chuvas fortes", -22.9733, -43.2242),

        // Minas Gerais
        PontoAlagamento("MG01", "Ouro Preto", "MG", TipoDeRiscoAlagamento.CRITICO, "Enchentes e deslizamentos em áreas históricas", -20.3850, -43.5042),
        PontoAlagamento("MG02", "Governador Valadares", "MG", TipoDeRiscoAlagamento.ELEVADO, "Grandes cheias do Rio Doce", -18.8519, -41.9494),
        PontoAlagamento("MG03", "Juiz de Fora", "MG", TipoDeRiscoAlagamento.ELEVADO, "Inundações do Rio Paraibuna", -21.7642, -43.3496),
        PontoAlagamento("MG04", "Contagem", "MG", TipoDeRiscoAlagamento.COMUM, "Inundações urbanas e de córregos", -19.9175, -44.0542),
        PontoAlagamento("MG05", "Itabirito", "MG", TipoDeRiscoAlagamento.ELEVADO, "Inundação pelo Rio Itabirito", -20.2536, -43.8017),
        PontoAlagamento("MG06", "Mariana", "MG", TipoDeRiscoAlagamento.CRITICO, "Risco de enchentes e rompimento de barragens", -20.3778, -43.4169),

        // Espírito Santo
        PontoAlagamento("ES01", "Vila Velha", "ES", TipoDeRiscoAlagamento.ELEVADO, "Alagamentos severos por chuvas e maré alta", -20.3395, -40.2925),
        PontoAlagamento("ES02", "Cariacica", "ES", TipoDeRiscoAlagamento.ELEVADO, "Inundações na bacia do Rio Marinho", -20.2647, -40.4208),
        PontoAlagamento("ES03", "Colatina", "ES", TipoDeRiscoAlagamento.ELEVADO, "Cheias do Rio Doce", -19.5394, -40.6311),
        PontoAlagamento("ES04", "Iconha", "ES", TipoDeRiscoAlagamento.CRITICO, "Histórico de enchentes devastadoras", -20.7878, -40.8064),
        PontoAlagamento("ES05", "Cachoeiro de Itapemirim", "ES", TipoDeRiscoAlagamento.COMUM, "Inundações do Rio Itapemirim", -20.8461, -41.1133)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Configuração da Toolbar, Drawer, etc.
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navigationView.setNavigationItemSelectedListener(this)
        populateDrawerMenu()

        // Configuração do BottomSheet
        val bottomSheetView = findViewById<View>(R.id.bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView)
        textViewSensorName = bottomSheetView.findViewById(R.id.textViewSensorName)
        textViewSensorId = bottomSheetView.findViewById(R.id.textViewSensorId)
        textViewWaterLevel = bottomSheetView.findViewById(R.id.textViewWaterLevel)
        textViewStatus = bottomSheetView.findViewById(R.id.textViewStatus)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        // Configuração dos Botões Flutuantes (FABs)
        fabToggleMapTheme = findViewById(R.id.fab_toggle_map_theme)
        fabToggleMapTheme.setOnClickListener { toggleMapTheme() }
        fabMyLocation = findViewById(R.id.fab_my_location)
        fabMyLocation.setOnClickListener { checkLocationPermissionAndGetLocation() }

        // Inicialização do cliente de localização e do mapa
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    private fun populateDrawerMenu() {
        val menu: Menu = navigationView.menu
        val group = menu.findItem(R.id.group_global_points)?.subMenu ?: menu
        group.removeGroup(R.id.group_global_points)

        listaPontosAlagamento.forEachIndexed { index, ponto ->
            // Alterado para mostrar a cidade e o estado
            group.add(R.id.group_global_points, index, Menu.NONE, "${ponto.nomeLocal}, ${ponto.estado}")
                .setIcon(R.drawable.ic_map_marker_outline)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        this.googleMap = map
        googleMap?.setInfoWindowAdapter(null)
        applyCurrentMapTheme()
        adicionarMarcadoresDosSensores()
        adicionarMarcadoresPontosAlagamento()

        googleMap?.setOnMarkerClickListener { marker ->
            val tag = marker.tag
            if (tag != null) {
                updateBottomSheet(tag)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
            true
        }
        googleMap?.setOnMapClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        checkLocationPermissionAndGetLocation()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // >>> FOCO INICIAL ALTERADO PARA A REGIÃO SUDESTE <<<
            val focoInicial = LatLng(-21.50, -44.50) // Ponto central na região Sudeste
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(focoInicial, 6f)) // Zoom para ver a região
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkLocationPermissionAndGetLocation() {
        googleMap ?: return
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                getDeviceLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        googleMap ?: return
        try {
            googleMap?.isMyLocationEnabled = true
            googleMap?.uiSettings?.isMyLocationButtonEnabled = false
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                } else {
                    Toast.makeText(this, "Não foi possível obter a última localização.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: SecurityException) {
            Log.e("SecurityException", "Permissão de localização negada.", e)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (item.groupId == R.id.group_global_points && itemId >= 0 && itemId < listaPontosAlagamento.size) {
            val pontoSelecionado = listaPontosAlagamento[itemId]
            val local = LatLng(pontoSelecionado.latitude, pontoSelecionado.longitude)
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(local, 12f))
            updateBottomSheet(pontoSelecionado)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun updateBottomSheet(tagDoMarcador: Any) {
        when (tagDoMarcador) {
            is Sensor -> {
                val sensor = tagDoMarcador
                textViewSensorName.text = sensor.nome
                textViewSensorId.text = "ID do Sensor: ${sensor.id}"
                textViewWaterLevel.text = "Nível da Água: ${sensor.nivelAguaMetros}m"
                when {
                    sensor.nivelAguaMetros >= 4.0 -> {
                        textViewStatus.text = "STATUS: PERIGO"
                        textViewStatus.setTextColor(Color.parseColor("#D32F2F"))
                        textViewStatus.setBackgroundResource(R.drawable.status_background_perigo)
                    }
                    sensor.nivelAguaMetros >= 2.5 -> {
                        textViewStatus.text = "STATUS: ALERTA"
                        textViewStatus.setTextColor(Color.parseColor("#FFA000"))
                        textViewStatus.setBackgroundResource(R.drawable.status_background_alerta)
                    }
                    else -> {
                        textViewStatus.text = "STATUS: NORMAL"
                        textViewStatus.setTextColor(Color.parseColor("#388E3C"))
                        textViewStatus.setBackgroundResource(R.drawable.status_background_normal)
                    }
                }
            }
            is PontoAlagamento -> {
                val ponto = tagDoMarcador
                textViewSensorName.text = "${ponto.nomeLocal}, ${ponto.estado}"
                textViewSensorId.text = "Risco: ${ponto.tipoRisco.name.lowercase().replaceFirstChar { it.titlecase() }}"
                textViewWaterLevel.text = ponto.descricaoBreve
                when (ponto.tipoRisco) {
                    TipoDeRiscoAlagamento.CRITICO -> {
                        textViewStatus.text = "RISCO CRÍTICO"
                        textViewStatus.setTextColor(Color.parseColor("#C51162"))
                        textViewStatus.setBackgroundResource(R.drawable.status_background_perigo)
                    }
                    TipoDeRiscoAlagamento.ELEVADO -> {
                        textViewStatus.text = "RISCO ELEVADO"
                        textViewStatus.setTextColor(Color.parseColor("#FF6F00"))
                        textViewStatus.setBackgroundResource(R.drawable.status_background_alerta)
                    }
                    else -> {
                        textViewStatus.text = "ALAGAMENTO COMUM"
                        textViewStatus.setTextColor(Color.parseColor("#00838F"))
                        textViewStatus.setBackgroundResource(R.drawable.status_background_normal)
                    }
                }
            }
        }
    }

    // O resto do código (onOptionsItemSelected, onBackPressed, toggleMapTheme, getCorDoMarcador, etc.)
    // continua o mesmo que na versão anterior.

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) { return true }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun toggleMapTheme() {
        isMapInDarkMode = !isMapInDarkMode
        applyCurrentMapTheme()
    }

    private fun applyCurrentMapTheme() {
        googleMap ?: return
        val styleId = if (isMapInDarkMode) {
            fabToggleMapTheme.setImageResource(R.drawable.ic_visibility_off_24dp)
            R.raw.dark_map_style
        } else {
            fabToggleMapTheme.setImageResource(R.drawable.ic_visibility_24dp)
            R.raw.map_style
        }
        try {
            val success = googleMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, styleId))
            if (!success) Log.e("MainActivity", "Falha ao aplicar novo estilo do mapa.")
        } catch (e: Exception) {
            Log.e("MainActivity", "Não foi possível carregar novo estilo do mapa.", e)
        }
    }

    private fun adicionarMarcadoresDosSensores() {
        googleMap ?: return
        listaDeSensores.forEach { sensor ->
            val posicaoSensor = LatLng(sensor.latitude, sensor.longitude)
            val corDoIcone = getCorDoMarcadorSensor(sensor.nivelAguaMetros)
            val marcador = googleMap!!.addMarker(
                MarkerOptions().position(posicaoSensor).title(sensor.nome).icon(corDoIcone)
            )
            marcador?.tag = sensor
        }
    }

    private fun adicionarMarcadoresPontosAlagamento() {
        googleMap ?: return
        listaPontosAlagamento.forEach { ponto ->
            val posicaoPonto = LatLng(ponto.latitude, ponto.longitude)
            val corDoIcone = getCorDoMarcadorPontoAlagamento(ponto.tipoRisco)
            val marcador = googleMap!!.addMarker(
                MarkerOptions().position(posicaoPonto).title(ponto.nomeLocal).icon(corDoIcone)
            )
            marcador?.tag = ponto
        }
    }

    private fun getCorDoMarcadorSensor(nivelAgua: Double): BitmapDescriptor {
        val tonalidade = when {
            nivelAgua >= 4.0 -> BitmapDescriptorFactory.HUE_RED
            nivelAgua >= 2.5 -> BitmapDescriptorFactory.HUE_YELLOW
            else -> BitmapDescriptorFactory.HUE_AZURE
        }
        return BitmapDescriptorFactory.defaultMarker(tonalidade)
    }

    private fun getCorDoMarcadorPontoAlagamento(tipoRisco: TipoDeRiscoAlagamento): BitmapDescriptor {
        val tonalidade = when (tipoRisco) {
            TipoDeRiscoAlagamento.CRITICO -> BitmapDescriptorFactory.HUE_MAGENTA
            TipoDeRiscoAlagamento.ELEVADO -> BitmapDescriptorFactory.HUE_ORANGE
            TipoDeRiscoAlagamento.COMUM -> BitmapDescriptorFactory.HUE_CYAN
        }
        return BitmapDescriptorFactory.defaultMarker(tonalidade)
    }

    // Métodos de ciclo de vida do MapView
    override fun onResume() { super.onResume(); if(::mapView.isInitialized) mapView.onResume() }
    override fun onStart() { super.onStart(); if(::mapView.isInitialized) mapView.onStart() }
    override fun onStop() { super.onStop(); if(::mapView.isInitialized) mapView.onStop() }
    override fun onPause() { super.onPause(); if(::mapView.isInitialized) mapView.onPause() }
    override fun onLowMemory() { super.onLowMemory(); if(::mapView.isInitialized) mapView.onLowMemory() }
    override fun onDestroy() {
        if(::mapView.isInitialized) mapView.onDestroy()
        super.onDestroy()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(::mapView.isInitialized) mapView.onSaveInstanceState(outState)
    }
}