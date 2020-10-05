package eu.alkismavridis.euljava.core.types


class NativeEulType(private val _name: String) : EulType {
    override fun getName() = this._name
}
